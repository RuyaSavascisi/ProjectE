package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.PotDecorations;
import org.jetbrains.annotations.NotNull;

@DataComponentProcessor//TODO - 1.21: Figure out if we should remove this, also figure out where the base value even comes from
public class DecoratedPotProcessor extends PersistentComponentProcessor<PotDecorations> {

	@DataComponentProcessor.Instance
	public static final DecoratedPotProcessor INSTANCE = new DecoratedPotProcessor();
	private static final ResourceKey<Item> DECORATED_POT = BuiltInRegistries.ITEM.getResourceKey(Items.DECORATED_POT).orElseThrow();

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_DECORATED_POT.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_DECORATED_POT.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_DECORATED_POT.tooltip();
	}

	@Override
	protected long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull PotDecorations decorations) throws ArithmeticException {
		long decorationEmc = 0;
		for (Item decoration : decorations.ordered()) {
			decorationEmc = Math.addExact(decorationEmc, EMCHelper.getEmcValue(decoration));
		}
		//Calculate base decorated pot (four bricks) emc to subtract from our current values
		// We do this in case this isn't the first processor that runs on some pot, and another processor has adjusted the emc of it
		return Math.addExact(currentEMC - EMCHelper.getEmcValue(Items.DECORATED_POT), decorationEmc);
	}

	@Override
	protected boolean validItem(@NotNull ItemInfo info) {
		return info.getItem().is(DECORATED_POT);
	}

	@Override
	protected DataComponentType<PotDecorations> getComponentType(@NotNull ItemInfo info) {
		return DataComponents.POT_DECORATIONS;
	}

	@Override
	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull PotDecorations component) {
		return !component.equals(PotDecorations.EMPTY);
	}
}
