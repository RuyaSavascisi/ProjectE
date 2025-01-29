package moze_intel.projecte.network.commands.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.HashSet;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.config.MappingConfig;
import moze_intel.projecte.emc.mappers.OreBlacklistMapper;
import moze_intel.projecte.emc.mappers.RawMaterialsBlacklistMapper;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.Tags;

public class DumpMissingEmc {

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
		return Commands.literal("dumpmissingemc")
				.then(Commands.argument("skip_expected", BoolArgumentType.bool())
						.executes(ctx -> execute(ctx, BoolArgumentType.getBool(ctx, "skip_expected")))
				).executes(ctx -> execute(ctx, false));
	}

	private static boolean expectedMissing(Holder<Item> item) {
		if (item.value() instanceof SpawnEggItem) {
			return true;
		} else if (item.is(Tags.Items.ORES) && MappingConfig.isEnabled(OreBlacklistMapper.INSTANCE)) {
			return true;
		} else if (item.is(Tags.Items.RAW_MATERIALS)&& MappingConfig.isEnabled(RawMaterialsBlacklistMapper.INSTANCE)) {
			return true;
		}
		//TODO - 1.21: Skip any other expected things? Should we just put them in a tag?
		return false;
	}

	private static int execute(CommandContext<CommandSourceStack> ctx, boolean skipExpectedMissing) {
		CommandSourceStack source = ctx.getSource();
		RegistryAccess registryAccess = source.registryAccess();
		CreativeModeTab tab = registryAccess.holderOrThrow(CreativeModeTabs.SEARCH).value();
		if (tab.getSearchTabDisplayItems().isEmpty()) {
			//If the search tab hasn't been initialized yet initialize it
			Minecraft minecraft = Minecraft.getInstance();
			boolean hasPermissions = minecraft.options.operatorItemsTab().get();
			if (!hasPermissions) {
				if (minecraft.player != null) {
					hasPermissions = minecraft.player.canUseGameMasterBlocks();
				} else {
					hasPermissions = source.hasPermission(Commands.LEVEL_GAMEMASTERS);
				}
			}

			//TODO - 1.21: Backport https://github.com/neoforged/NeoForge/pull/1928 and use it?
			FeatureFlagSet features = minecraft.getConnection() == null ? FeatureFlags.DEFAULT_FLAGS : minecraft.getConnection().enabledFeatures();
			CreativeModeTab.ItemDisplayParameters displayParameters = new CreativeModeTab.ItemDisplayParameters(features, hasPermissions, registryAccess);
			try {
				tab.buildContents(displayParameters);
			} catch (Exception ignored) {
				//We can't initialize yet for some reason, so we will just end up falling back to base items only
			}
		}

		Set<ItemInfo> missing = new HashSet<>();
		for (Item item : registryAccess.registryOrThrow(Registries.ITEM)) {
			if (item != Items.AIR) {
				//noinspection deprecation
				if (skipExpectedMissing && expectedMissing(item.builtInRegistryHolder())) {
					//Skip any items that we expected to be missing (for example ores)
					continue;
				}
				//Note: This is intentionally not using Item#getDefaultInstance as data component based variants should be based on the creative mode tabs
				ItemInfo itemInfo = ItemInfo.fromItem(item);
				if (!EMCHelper.doesItemHaveEmc(itemInfo)) {
					//If the item isn't air and doesn't have EMC add it to the list of items we haven't addressed yet
					missing.add(itemInfo);
				}
			}
		}
		//Check all items in the search tab to see if they have an EMC value (as they may have data component variants declared)
		for (ItemStack stack : tab.getSearchTabDisplayItems()) {
			if (!stack.isEmpty() && !stack.isComponentsPatchEmpty()) {
				//If the stack is not empty, and it has non defaulted components: see if any of the added variants have EMC
				ItemInfo itemInfo = ItemInfo.fromStack(stack);
				if (EMCHelper.doesItemHaveEmc(itemInfo)) {
					//If it does, remove the default variant from missing if it was missing
					missing.remove(itemInfo.itemOnly());
				} else {
					//If it doesn't, add it to the set of items that are missing an EMC value
					missing.add(itemInfo);
				}
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
}