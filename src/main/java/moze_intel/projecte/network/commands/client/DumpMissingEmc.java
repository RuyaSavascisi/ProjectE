package moze_intel.projecte.network.commands.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.HashSet;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.config.MappingConfig;
import moze_intel.projecte.emc.mappers.OreBlacklistMapper;
import moze_intel.projecte.emc.mappers.RawMaterialsBlacklistMapper;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public class DumpMissingEmc {

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
		return Commands.literal("dumpmissingemc")
				.then(Commands.argument("skip_expected", BoolArgumentType.bool())
						.executes(ctx -> execute(ctx, BoolArgumentType.getBool(ctx, "skip_expected")))
				).executes(ctx -> execute(ctx, false));
	}

	private static boolean expectedMissing(Item item) {
		if (item instanceof BlockItem blockItem && blockItem.getBlock().defaultDestroyTime() == -1) {
			//Assume unbreakable blocks won't have an EMC value by default
			return true;
		}
		//noinspection deprecation
		Reference<Item> holder = item.builtInRegistryHolder();
		if (MappingConfig.isEnabled(OreBlacklistMapper.INSTANCE)) {
			if (holder.is(Tags.Items.ORES) || item == Items.GILDED_BLACKSTONE) {
				return true;
			}
		}
		if (MappingConfig.isEnabled(RawMaterialsBlacklistMapper.INSTANCE)) {
			if (holder.is(Tags.Items.RAW_MATERIALS) || holder.is(Tags.Items.STORAGE_BLOCKS_RAW_COPPER) ||
				holder.is(Tags.Items.STORAGE_BLOCKS_RAW_IRON) || holder.is(Tags.Items.STORAGE_BLOCKS_RAW_GOLD)) {
				return true;
			}
		}
		return holder.is(PETags.Items.IGNORE_MISSING_EMC);
		//TODO - 1.21: Add values for or mark as ignored
		// minecraft:bolt_armor_trim_smithing_template
		// minecraft:flow_armor_trim_smithing_template
		// minecraft:flow_banner_pattern
		// minecraft:guster_banner_pattern
		// minecraft:heavy_core // minecraft:mace
		// minecraft:ominous_bottle
		// minecraft:ominous_bottle {minecraft:ominous_bottle_amplifier=>1}
		// minecraft:ominous_bottle {minecraft:ominous_bottle_amplifier=>2}
		// minecraft:ominous_bottle {minecraft:ominous_bottle_amplifier=>3}
		// minecraft:ominous_bottle {minecraft:ominous_bottle_amplifier=>4}
		// minecraft:firework_star
	}

	private static int execute(CommandContext<CommandSourceStack> ctx, boolean skipExpectedMissing) {
		CommandSourceStack source = ctx.getSource();
		RegistryAccess registryAccess = source.registryAccess();
		Minecraft minecraft = Minecraft.getInstance();
		//TODO - 1.21.4: Make use of https://github.com/neoforged/NeoForge/pull/1928
		FeatureFlagSet features = minecraft.getConnection() == null ? FeatureFlags.DEFAULT_FLAGS : minecraft.getConnection().enabledFeatures();
		CreativeModeTab tab = registryAccess.holderOrThrow(CreativeModeTabs.SEARCH).value();
		if (tab.getSearchTabDisplayItems().isEmpty()) {
			//If the search tab hasn't been initialized yet initialize it
			boolean hasPermissions = minecraft.options.operatorItemsTab().get();
			if (!hasPermissions) {
				if (minecraft.player != null) {
					hasPermissions = minecraft.player.canUseGameMasterBlocks();
				} else {
					hasPermissions = source.hasPermission(Commands.LEVEL_GAMEMASTERS);
				}
			}

			try {
				tab.buildContents(new CreativeModeTab.ItemDisplayParameters(features, hasPermissions, registryAccess));
			} catch (Exception ignored) {
				//We can't initialize yet for some reason, so we will just end up falling back to base items only
			}
		}

		Set<ItemInfo> missing = new HashSet<>();
		for (Item item : registryAccess.registryOrThrow(Registries.ITEM)) {
			//Skip air, and skip any items that are not currently enabled in the world
			if (item != Items.AIR && item.isEnabled(features)) {
				if (skipExpectedMissing && expectedMissing(item)) {
					//Skip any items that we expected to be missing (for example ores)
					continue;
				}
				//Note: This is intentionally not using Item#getDefaultInstance as data component based variants should be based on the creative mode tabs
				ItemInfo itemInfo = ItemInfo.fromItem(item);
				if (!IEMCProxy.INSTANCE.hasValue(itemInfo)) {
					//If the item doesn't have EMC add it to the list of items we haven't addressed yet
					missing.add(itemInfo);
				}
			}
		}
		//Check all items in the search tab to see if they have an EMC value (as they may have data component variants declared)
		for (ItemStack stack : tab.getSearchTabDisplayItems()) {
			if (!stack.isEmpty() && !stack.isComponentsPatchEmpty()) {
				//If the stack is not empty, and it has non defaulted components: see if any of the added variants have EMC
				ItemInfo itemInfo = ItemInfo.fromStack(stack);
				if (IEMCProxy.INSTANCE.hasValue(itemInfo)) {
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
			missing.stream()
					.map(ItemInfo::toString)
					.sorted()
					.forEach(PECore.LOGGER::info);
		}
		return missingCount;
	}
}