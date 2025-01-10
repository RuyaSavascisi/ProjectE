package moze_intel.projecte.network.commands.client;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DumpMissingEmc {

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
		return Commands.literal("dumpmissingemc")
				.executes(DumpMissingEmc::execute);
	}

	private static int execute(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		//TODO - 1.21: Test this
		Set<Holder<Item>> allItems = BuiltInRegistries.ITEM.holders().collect(Collectors.toSet());
		allItems.remove(Items.AIR.builtInRegistryHolder());//Ignore air
		Set<ItemInfo> missing = new HashSet<>();
		CreativeModeTab tab = CreativeModeTabs.searchTab();
		if (tab.getSearchTabDisplayItems().isEmpty()) {
			//If the search tab hasn't been initialized yet initialize it
			initTab(tab);
		}
		//Check all items in the search tab to see if they have an EMC value (as they may have data component variants declared)
		for (ItemStack stack : tab.getSearchTabDisplayItems()) {
			if (!stack.isEmpty()) {
				ItemInfo itemInfo = ItemInfo.fromStack(stack);
				if (EMCHelper.getEmcValue(itemInfo) == 0) {
					missing.add(itemInfo);
				} else {
					allItems.remove(stack.getItemHolder());
				}
			}
		}
		for (Holder<Item> item : allItems) {
			//Try any items that we didn't have a variant with non default data components for that had one
			//Note: This is intentionally not using Item#getDefaultInstance as data component based variants should be based on the creative mode tabs
			ItemInfo itemInfo = ItemInfo.fromItem(item);
			if (EMCHelper.getEmcValue(itemInfo) == 0) {
				missing.add(itemInfo);
			}
		}
		int missingCount = missing.size();
		if (missingCount == 0) {
			source.sendSuccess(PELang.DUMP_MISSING_EMC_NONE_MISSING::translate, true);
		} else {
			if (missingCount == 1) {
				source.sendSuccess(PELang.DUMP_MISSING_EMC_ONE_MISSING::translate, true);
			} else {
				source.sendSuccess(() -> PELang.DUMP_MISSING_EMC_MULTIPLE_MISSING.translate(missingCount), true);
			}
			for (ItemInfo itemInfo : missing) {
				PECore.LOGGER.info(itemInfo.toString());
			}
		}
		return missingCount;
	}

	private static void initTab(CreativeModeTab tab) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null) {
			FeatureFlagSet features = Optional.ofNullable(minecraft.player)
					.map(p -> p.connection.enabledFeatures())
					.orElse(FeatureFlags.DEFAULT_FLAGS);
			boolean hasPermissions = minecraft.options.operatorItemsTab().get() || minecraft.player != null && minecraft.player.canUseGameMasterBlocks();
			CreativeModeTab.ItemDisplayParameters displayParameters = new CreativeModeTab.ItemDisplayParameters(features, hasPermissions, minecraft.level.registryAccess());
			try {
				tab.buildContents(displayParameters);
			} catch (RuntimeException | LinkageError ignored) {
				//We can't initialize yet for some reason, so we will just end up falling back to base items only
			}
		}
	}
}