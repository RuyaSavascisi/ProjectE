package moze_intel.projecte.emc.components;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToLongFunction;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.config.MappingConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.utils.AnnotationHelper;
import moze_intel.projecte.utils.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class DataComponentManager {

	private static final List<IDataComponentProcessor> processors = new ArrayList<>();
	@Nullable
	private static Object2LongMap<DyeColor> colorEmc;

	public static List<IDataComponentProcessor> loadProcessors() {
		if (processors.isEmpty()) {
			processors.addAll(AnnotationHelper.getDataComponentProcessors());
		}
		return Collections.unmodifiableList(processors);
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getColorEmc(@NotNull DyeColor color) {
		return colorEmc == null ? 0 : colorEmc.getLong(color);
	}

	//TODO - 1.21: Do we want to expose this as a helper
	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getMinEmcFor(ToLongFunction<ItemInfo> emcLookup, Ingredient ingredient) {
		try {
			long minEmc = 0;
			for (ItemStack stack : ingredient.getItems()) {
				if (!stack.isEmpty()) {
					long emc = emcLookup.applyAsLong(ItemInfo.fromStack(stack));
					if (emc != 0 && (minEmc == 0 || emc < minEmc)) {
						minEmc = emc;
					}
				}
			}
			return minEmc;
		} catch (Exception e) {
			//TODO - 1.21: Log an error, even though theoretically there shouldn't be any exceptions with how late we query the ingredient
			return 0;
		}
	}

	public static void updateCachedValues(@Nullable ToLongFunction<ItemInfo> emcLookup) {
		//TODO - 1.21: Log the time it takes to do this?
		if (emcLookup == null) {
			colorEmc = null;
		} else {
			//TODO - 1.21: Test this
			//Calculate and store the min emc value needed for specific dye colors for use in data component processors
			//TODO: Do we want to eventually try and make this support ProjectEAPI.FREE_ARITHMETIC_VALUE
			colorEmc = new Object2LongOpenHashMap<>();
			for (DyeColor color : Constants.COLORS) {
				long minColorEmc = 0;
				for (Holder<Item> dye : BuiltInRegistries.ITEM.getTagOrEmpty(color.getTag())) {
					long dyeEmc = emcLookup.applyAsLong(ItemInfo.fromItem(dye));
					if (dyeEmc != 0 && (minColorEmc == 0 || dyeEmc < minColorEmc)) {
						minColorEmc = dyeEmc;
					}
				}
				if (minColorEmc > 0) {
					colorEmc.put(color, minColorEmc);
				}
			}
		}
		for (IDataComponentProcessor processor : processors) {
			//TODO - 1.21: Do we want to fire this even when the processor is disabled in case it gets enabled later?
			// That or we need to add in some callbacks for when processor enabled state changes
			if (MappingConfig.isEnabled(processor)) {
				processor.updateCachedValues(emcLookup);
			}
		}

	}

	@NotNull
	public static ItemInfo getPersistentInfo(@NotNull ItemInfo info) {
		if (!info.hasModifiedComponents() || info.getItem().is(PETags.Items.DATA_COMPONENT_WHITELIST) || EMCMappingHandler.hasEmcValue(info)) {
			//If we have no custom Data Components, we want to allow data components to be kept, or we have an exact match to a stored value just go with it
			return info;
		}
		//Cleans up the tag in item to reduce it as much as possible
		DataComponentPatch.Builder builder = DataComponentPatch.builder();
		for (IDataComponentProcessor processor : processors) {
			if (MappingConfig.isEnabled(processor) && processor.hasPersistentComponents() && MappingConfig.hasPersistent(processor)) {
				processor.collectPersistentComponents(info, builder);
			}
		}
		return ItemInfo.fromItem(info.getItem(), builder.build());
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcValue(@NotNull ItemInfo info) {
		//TODO: Fix this, as it does not catch the edge case that we have an exact match and then there are random added Data Components on top of it
		// but that can be thought about more once we have the first pass complete. For example if someone put an enchantment on a potion
		long emcValue = EMCMappingHandler.getStoredEmcValue(info);
		if (!info.hasModifiedComponents()) {
			//If our item has no custom Data Components anyway, just return based on the value we got for it
			return emcValue;
		} else if (emcValue == 0) {
			//Try getting a base emc value from the Data Component less variant if we don't have one matching our Data Components
			emcValue = EMCMappingHandler.getStoredEmcValue(info.itemOnly());
			if (emcValue == 0) {
				//The base item doesn't have an EMC value either so just exit
				return 0;
			}
		}

		//Note: We continue to use our initial ItemInfo so that we are calculating based on the Data Components
		for (IDataComponentProcessor processor : processors) {
			if (MappingConfig.isEnabled(processor)) {
				try {
					emcValue = processor.recalculateEMC(info, emcValue);
				} catch (ArithmeticException e) {
					//Return the last successfully calculated EMC value
					//TODO - 1.21: Given this likely means it overflowed, we probably want to instead return zero so that they don't get a loss of emc
					//return emcValue;
					return 0;
				}
				if (emcValue <= 0) {
					//Exit if it gets to zero (also safety check for less than zero in case a mod didn't bother sanctifying their data)
					return 0;
				}
			}
		}
		return emcValue;
	}
}