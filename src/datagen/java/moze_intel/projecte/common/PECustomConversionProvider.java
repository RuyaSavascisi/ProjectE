package moze_intel.projecte.common;

import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.data.ConversionGroupBuilder;
import moze_intel.projecte.api.data.CustomConversionBuilder;
import moze_intel.projecte.api.data.CustomConversionProvider;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class PECustomConversionProvider extends CustomConversionProvider {

	public PECustomConversionProvider(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider);
	}

	@Override
	protected void addCustomConversions(@NotNull HolderLookup.Provider registries) {
		createConversionBuilder(PECore.rl("metals"))
				.comment("Sets default conversions for various metals from other mods and their default values.")
				.before(Tags.Items.INGOTS_IRON, 256)
				.before(ingotTag("uranium"), 4_096)
				.before(Tags.Items.GEMS_AMETHYST, 32)
				.before(gemTag("ruby"), 2_048)
				.before(gemTag("sapphire"), 2_048)
				.before(gemTag("peridot"), 2_048)
				.conversion(Tags.Items.INGOTS_GOLD).ingredient(Tags.Items.INGOTS_IRON, 8).propagateTags().end()
				.conversion(Tags.Items.INGOTS_COPPER, 2).ingredient(Tags.Items.INGOTS_IRON).propagateTags().end()
				.conversion(ingotTag("tin")).ingredient(Tags.Items.INGOTS_IRON).propagateTags().end()
				.conversion(ingotTag("bronze"), 4).ingredient(ingotTag("copper"), 3).ingredient(ingotTag("tin")).propagateTags().end()
				.conversion(ingotTag("silver")).ingredient(Tags.Items.INGOTS_IRON, 2).propagateTags().end()
				.conversion(ingotTag("lead")).ingredient(Tags.Items.INGOTS_IRON, 2).propagateTags().end()
				.conversion(ingotTag("osmium")).ingredient(Tags.Items.INGOTS_IRON, 2).propagateTags().end()
				.conversion(ingotTag("nickel")).ingredient(Tags.Items.INGOTS_IRON, 4).propagateTags().end()
				.conversion(ingotTag("aluminum"), 2).ingredient(Tags.Items.INGOTS_IRON).propagateTags().end()
				.conversion(ingotTag("platinum")).ingredient(Tags.Items.INGOTS_IRON, 16).propagateTags().end()
				.conversion(ingotTag("cyanite"), 4).ingredient(ingotTag("uranium")).propagateTags().end()
		;
		NormalizedSimpleStack singleEMC = NSSFake.create("single_emc");
		ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
		CustomConversionBuilder defaultBuilder = createConversionBuilder(PECore.rl("defaults"))
				.comment("Default values for vanilla items.")
				.group("default")
				.comment("Default conversion group.")
				.conversion(Items.GRASS_BLOCK).ingredient(Items.DIRT, 2).end()
				.conversion(Items.PODZOL).ingredient(Items.DIRT, 2).end()
				.conversion(Items.ROOTED_DIRT).ingredient(Items.DIRT).ingredient(Items.HANGING_ROOTS).end()
				.conversion(Items.MYCELIUM).ingredient(Items.DIRT, 2).end()
				.conversion(Items.MUD).ingredient(Items.DIRT).ingredient(waterBottle).ingredient(Items.GLASS_BOTTLE, -1).end()
				.conversion(Items.CRIMSON_NYLIUM).ingredient(Items.NETHERRACK, 2).end()
				.conversion(Items.WARPED_NYLIUM).ingredient(Items.NETHERRACK, 2).end()
				.conversion(Items.IRON_HORSE_ARMOR).ingredient(Tags.Items.INGOTS_IRON, 8).end()
				.conversion(Items.GOLDEN_HORSE_ARMOR).ingredient(Tags.Items.INGOTS_GOLD, 8).end()
				.conversion(Items.DIAMOND_HORSE_ARMOR).ingredient(Tags.Items.GEMS_DIAMOND, 8).end()
				.conversion(Items.DISC_FRAGMENT_5, 9).ingredient(Items.MUSIC_DISC_5).end()
				.conversion(Items.CARVED_PUMPKIN).ingredient(Items.PUMPKIN).end()
				.conversion(Items.ENCHANTED_BOOK).ingredient(Items.BOOK).end()
				.conversion(Items.ENCHANTED_GOLDEN_APPLE).ingredient(Items.APPLE).ingredient(Tags.Items.STORAGE_BLOCKS_GOLD, 8).end()
				.conversion(Items.WET_SPONGE).ingredient(Items.SPONGE).end()
				.conversion(Items.BELL).ingredient(Tags.Items.INGOTS_GOLD, 7).end()
				.conversion(Items.HONEYCOMB, 3).ingredient(Items.HONEY_BOTTLE).end()
				.conversion(Items.POWDER_SNOW_BUCKET).ingredient(Items.BUCKET).ingredient(Items.SNOW_BLOCK, 4).end()
				.conversion(Items.GLOW_INK_SAC).ingredient(Items.INK_SAC).ingredient(Tags.Items.DUSTS_GLOWSTONE).end()
				.conversion(Items.HANGING_ROOTS).ingredient(Tags.Items.RODS_WOODEN).end()
				.conversion(Items.SUSPICIOUS_STEW)
				.ingredient(Items.BOWL)
				.ingredient(Items.BROWN_MUSHROOM)
				.ingredient(Items.RED_MUSHROOM)
				.ingredient(ItemTags.SMALL_FLOWERS)
				.end()
				.conversion(Items.PUFFERFISH_BUCKET).ingredient(Items.WATER_BUCKET).ingredient(Items.PUFFERFISH).end()
				.conversion(Items.SALMON_BUCKET).ingredient(Items.WATER_BUCKET).ingredient(Items.SALMON).end()
				.conversion(Items.COD_BUCKET).ingredient(Items.WATER_BUCKET).ingredient(Items.COD).end()
				.conversion(Items.TROPICAL_FISH_BUCKET).ingredient(Items.WATER_BUCKET).ingredient(Items.TROPICAL_FISH).end()
				.conversion(Items.DRAGON_BREATH).ingredient(Items.GLASS_BOTTLE).ingredient(singleEMC).end()
				.conversion(Items.SOUL_SOIL).ingredient(Items.SOUL_SAND).end()
				.conversion(Items.WARPED_WART_BLOCK).ingredient(Items.NETHER_WART_BLOCK).end()
				.conversion(Items.SHROOMLIGHT).ingredient(Items.GLOWSTONE_DUST).ingredient(Tags.Items.MUSHROOMS).end()
				.conversion(Items.OCHRE_FROGLIGHT, 3).ingredient(Items.MAGMA_CREAM).end()
				.conversion(Items.PEARLESCENT_FROGLIGHT, 3).ingredient(Items.MAGMA_CREAM).end()
				.conversion(Items.VERDANT_FROGLIGHT, 3).ingredient(Items.MAGMA_CREAM).end()
				.conversion(Items.SCULK).ingredient(Items.SCULK_VEIN, 4).end()
				.conversion(Items.SCULK_SENSOR).ingredient(Items.COMPARATOR, 4).ingredient(Items.REPEATER, 4).ingredient(Items.SCULK, 1).end()
				.conversion(Items.SCULK_SHRIEKER).ingredient(Items.SCULK_CATALYST, 4).end()
				.end()
				.group("dirt_path")
				.comment("Adds the various valid conversions into dirt paths.")
				//Dirt paths can be made from a variety of blocks
				.conversion(Items.DIRT_PATH).ingredient(Items.GRASS_BLOCK).end()
				.conversion(Items.DIRT_PATH).ingredient(Items.DIRT).end()
				.conversion(Items.DIRT_PATH).ingredient(Items.PODZOL).end()
				.conversion(Items.DIRT_PATH).ingredient(Items.COARSE_DIRT).end()
				.conversion(Items.DIRT_PATH).ingredient(Items.MYCELIUM).end()
				.conversion(Items.DIRT_PATH).ingredient(Items.ROOTED_DIRT).end()
				.end()
				.group("chainmail")
				.comment("Propagate iron armor values to chainmail armor as they are close enough that their difficulties balance out.")
				.conversion(Items.CHAINMAIL_HELMET).ingredient(Items.IRON_HELMET).end()
				.conversion(Items.CHAINMAIL_CHESTPLATE).ingredient(Items.IRON_CHESTPLATE).end()
				.conversion(Items.CHAINMAIL_LEGGINGS).ingredient(Items.IRON_LEGGINGS).end()
				.conversion(Items.CHAINMAIL_BOOTS).ingredient(Items.IRON_BOOTS).end()
				.end()
				.group("concrete_powder_to_block")
				.comment("Propagate concrete powder values to concrete blocks.")
				.conversion(Items.WHITE_CONCRETE).ingredient(Items.WHITE_CONCRETE_POWDER).end()
				.conversion(Items.ORANGE_CONCRETE).ingredient(Items.ORANGE_CONCRETE_POWDER).end()
				.conversion(Items.MAGENTA_CONCRETE).ingredient(Items.MAGENTA_CONCRETE_POWDER).end()
				.conversion(Items.LIGHT_BLUE_CONCRETE).ingredient(Items.LIGHT_BLUE_CONCRETE_POWDER).end()
				.conversion(Items.YELLOW_CONCRETE).ingredient(Items.YELLOW_CONCRETE_POWDER).end()
				.conversion(Items.LIME_CONCRETE).ingredient(Items.LIME_CONCRETE_POWDER).end()
				.conversion(Items.PINK_CONCRETE).ingredient(Items.PINK_CONCRETE_POWDER).end()
				.conversion(Items.GRAY_CONCRETE).ingredient(Items.GRAY_CONCRETE_POWDER).end()
				.conversion(Items.LIGHT_GRAY_CONCRETE).ingredient(Items.LIGHT_GRAY_CONCRETE_POWDER).end()
				.conversion(Items.CYAN_CONCRETE).ingredient(Items.CYAN_CONCRETE_POWDER).end()
				.conversion(Items.PURPLE_CONCRETE).ingredient(Items.PURPLE_CONCRETE_POWDER).end()
				.conversion(Items.BLUE_CONCRETE).ingredient(Items.BLUE_CONCRETE_POWDER).end()
				.conversion(Items.BROWN_CONCRETE).ingredient(Items.BROWN_CONCRETE_POWDER).end()
				.conversion(Items.GREEN_CONCRETE).ingredient(Items.GREEN_CONCRETE_POWDER).end()
				.conversion(Items.RED_CONCRETE).ingredient(Items.RED_CONCRETE_POWDER).end()
				.conversion(Items.BLACK_CONCRETE).ingredient(Items.BLACK_CONCRETE_POWDER).end()
				.end()
				.group("damaged_anvil")
				.comment("Calculates values for chipped and damaged anvils based on the average of surviving for 25 uses.")
				//Rough values based on how we factor damage into item EMC based on an average of ~8.3 uses before
				// it degrades a tier, we use 9 so the numbers are slightly worse for how efficiently it translates down
				.conversion(Items.CHIPPED_ANVIL, 25).ingredient(Items.ANVIL, 16).end()
				.conversion(Items.DAMAGED_ANVIL, 25).ingredient(Items.ANVIL, 7).end()
				.end()
				.group("fluid")
				.comment("Calculates the costs of filled buckets and some fluids.")
				.conversionFluid(FluidTags.LAVA, FluidType.BUCKET_VOLUME).ingredient(Items.OBSIDIAN).end()
				.conversionFluid(Tags.Fluids.MILK, FluidType.BUCKET_VOLUME).ingredient(singleEMC, 16).end()//One bucket worth of milk is 16 emc
				.conversion(Items.WATER_BUCKET).ingredient(Items.BUCKET).ingredientFluid(FluidTags.WATER, FluidType.BUCKET_VOLUME).end()
				.conversion(Items.LAVA_BUCKET).ingredient(Items.BUCKET).ingredientFluid(FluidTags.LAVA, FluidType.BUCKET_VOLUME).end()
				.conversion(Items.MILK_BUCKET).ingredient(Items.BUCKET).ingredientFluid(Fluids.MILK, FluidType.BUCKET_VOLUME).end()
				.end()
				.before(singleEMC, 1)
				.beforeFluid(FluidTags.WATER)
				.before(Items.COBBLESTONE, 1)
				.before(Items.GRANITE, 16)
				.before(Items.DIORITE, 16)
				.before(Items.ANDESITE, 16)
				.before(Items.POINTED_DRIPSTONE, 16)
				.before(Items.END_STONE, 1)
				.before(Items.NETHERRACK, 1)
				.before(Items.BASALT, 4)
				.before(Items.BLACKSTONE, 4)
				.before(Items.COBBLED_DEEPSLATE, 2)
				.before(Items.TUFF, 4)
				.before(Items.CALCITE, 32)
				.before(Items.DIRT, 1)
				.before(Items.SAND, 1)
				.before(Items.RED_SAND, 1)
				.before(Items.SNOW, 1)
				.before(Items.ICE, 1)
				.before(Items.DEAD_BUSH, 1)
				.before(Items.GRAVEL, 4)
				.before(Items.CACTUS, 8)
				.before(Items.VINE, 8)
				.before(Items.MOSS_BLOCK, 12)
				.before(Items.COBWEB, 12)
				.before(Items.LILY_PAD, 16)
				.before(Items.SMALL_DRIPLEAF, 24)
				.before(Items.BIG_DRIPLEAF, 32)
				.before(ItemTags.SMALL_FLOWERS, 16)
				.before(ItemTags.TALL_FLOWERS, 32)
				.before(Items.RED_MUSHROOM, 32)
				.before(Items.BROWN_MUSHROOM, 32)
				.before(Items.SUGAR_CANE, 32)
				.before(Items.BAMBOO, 32)
				.before(Items.SOUL_SAND, 49)
				.before(Items.OBSIDIAN, 64)
				.before(Items.CRYING_OBSIDIAN, 768)
				.before(Items.SPONGE, 128)
				.before(Items.GRASS, 1)
				.before(Items.SEAGRASS, 1)
				.before(Items.KELP, 1)
				.before(Items.SEA_PICKLE, 16)
				.before(Items.TALL_GRASS, 1)
				.before(Items.FERN, 1)
				.before(Items.LARGE_FERN, 1)
				.before(Items.MAGMA_BLOCK, 128)
				.before(Items.NETHER_SPROUTS, 1)
				.before(Items.CRIMSON_ROOTS, 1)
				.before(Items.WARPED_ROOTS, 1)
				.before(Items.WEEPING_VINES, 8)
				.before(Items.TWISTING_VINES, 8)
				.before(Items.GLOW_LICHEN, 8)
				.before(Items.CRIMSON_FUNGUS, 32)
				.before(Items.WARPED_FUNGUS, 32)
				.before(Items.SPORE_BLOSSOM, 64)
				.before(Items.TUBE_CORAL_BLOCK, 64)
				.before(Items.BRAIN_CORAL_BLOCK, 64)
				.before(Items.BUBBLE_CORAL_BLOCK, 64)
				.before(Items.FIRE_CORAL_BLOCK, 64)
				.before(Items.HORN_CORAL_BLOCK, 64)
				.before(Items.DEAD_TUBE_CORAL_BLOCK, 4)
				.before(Items.DEAD_BRAIN_CORAL_BLOCK, 4)
				.before(Items.DEAD_BUBBLE_CORAL_BLOCK, 4)
				.before(Items.DEAD_FIRE_CORAL_BLOCK, 4)
				.before(Items.DEAD_HORN_CORAL_BLOCK, 4)
				.before(Items.TUBE_CORAL_FAN, 16)
				.before(Items.BRAIN_CORAL_FAN, 16)
				.before(Items.BUBBLE_CORAL_FAN, 16)
				.before(Items.FIRE_CORAL_FAN, 16)
				.before(Items.HORN_CORAL_FAN, 16)
				.before(Items.DEAD_TUBE_CORAL_FAN, 1)
				.before(Items.DEAD_BRAIN_CORAL_FAN, 1)
				.before(Items.DEAD_BUBBLE_CORAL_FAN, 1)
				.before(Items.DEAD_FIRE_CORAL_FAN, 1)
				.before(Items.DEAD_HORN_CORAL_FAN, 1)
				.before(Items.TUBE_CORAL, 16)
				.before(Items.BRAIN_CORAL, 16)
				.before(Items.BUBBLE_CORAL, 16)
				.before(Items.FIRE_CORAL, 16)
				.before(Items.HORN_CORAL, 16)
				.before(Items.DEAD_TUBE_CORAL, 1)
				.before(Items.DEAD_BRAIN_CORAL, 1)
				.before(Items.DEAD_BUBBLE_CORAL, 1)
				.before(Items.DEAD_FIRE_CORAL, 1)
				.before(Items.DEAD_HORN_CORAL, 1)
				.before(Items.CHORUS_PLANT, 64)
				.before(Items.CHORUS_FLOWER, 96)
				.before(Items.CHORUS_FRUIT, 192)
				.before(Items.SCULK_VEIN, 4)
				.before(Items.SCULK_CATALYST, 8_040)
				.before(Tags.Items.SEEDS_WHEAT, 16)
				.before(Tags.Items.SEEDS_BEETROOT, 16)
				.before(Items.MELON_SLICE, 16)
				.before(Items.SWEET_BERRIES, 16)
				.before(Items.GLOW_BERRIES, 16)
				.before(Tags.Items.CROPS_WHEAT, 24)
				.before(Tags.Items.CROPS_NETHER_WART, 24)
				.before(Items.APPLE, 128)
				.before(Items.PUMPKIN, 144)
				.before(Items.HONEY_BOTTLE, 48)
				.before(Items.PORKCHOP, 64)
				.before(Items.BEEF, 64)
				.before(Items.CHICKEN, 64)
				.before(Items.RABBIT, 64)
				.before(Items.MUTTON, 64)
				.before(Items.COD, 64)
				.before(Items.SALMON, 64)
				.before(Items.TROPICAL_FISH, 64)
				.before(Items.PUFFERFISH, 64)
				.before(Tags.Items.CROPS_CARROT, 64)
				.before(Tags.Items.CROPS_BEETROOT, 64)
				.before(Tags.Items.CROPS_POTATO, 64)
				.before(Items.POISONOUS_POTATO, 64)
				.before(Items.STRING, 12)
				.before(Items.ROTTEN_FLESH, 32)
				.before(Items.SLIME_BALL, 32)
				.before(Items.EGG, 32)
				.before(Items.SCUTE, 96)
				.before(Items.TURTLE_EGG, 192)
				//Regular horns
				.before(horn(registries, Instruments.PONDER_GOAT_HORN), 96)
				.before(horn(registries, Instruments.SING_GOAT_HORN), 96)
				.before(horn(registries, Instruments.SEEK_GOAT_HORN), 96)
				.before(horn(registries, Instruments.FEEL_GOAT_HORN), 96)
				//Screaming horns
				.before(horn(registries, Instruments.ADMIRE_GOAT_HORN), 192)
				.before(horn(registries, Instruments.CALL_GOAT_HORN), 192)
				.before(horn(registries, Instruments.YEARN_GOAT_HORN), 192)
				.before(horn(registries, Instruments.DREAM_GOAT_HORN), 192)
				.before(Items.FEATHER, 48)
				.before(Items.RABBIT_HIDE, 16)
				.before(Items.RABBIT_FOOT, 128)
				.before(Items.SPIDER_EYE, 128)
				.before(Items.PHANTOM_MEMBRANE, 192)
				.before(Items.GUNPOWDER, 192)
				.before(Items.SKELETON_SKULL, 256)
				.before(Items.ZOMBIE_HEAD, 256)
				.before(Items.CREEPER_HEAD, 256)
				.before(Items.ENDER_PEARL, 1_024)
				.before(Items.NAUTILUS_SHELL, 1_024)
				.before(Items.BLAZE_ROD, 1_536)
				.before(Items.SHULKER_SHELL, 2_048)
				.before(Items.GHAST_TEAR, 4_096)
				.before(Items.TRIDENT, 16_398)
				.before(Items.HEART_OF_THE_SEA, 32_768)
				.before(Items.DRAGON_EGG, 262_144)
				.before(Items.SADDLE, 192)
				.before(Items.ECHO_SHARD, 192)
				.before(Items.NAME_TAG, 192)
				.before(ItemTags.MUSIC_DISCS, 2_048)
				.before(Items.FLINT, 4)
				.before(Items.COAL, 128)
				.before(Tags.Items.GEMS_QUARTZ, 256)
				.before(Items.PRISMARINE_SHARD, 256)
				.before(Items.PRISMARINE_CRYSTALS, 512)
				.before(Items.INK_SAC, 16)
				.before(Items.COCOA_BEANS, 64)
				.before(Items.LAPIS_LAZULI, 864)
				.before(Tags.Items.GEMS_EMERALD, 16_384)
				.before(Tags.Items.NETHER_STARS, 139_264)
				.before(Items.CLAY_BALL, 16)
				.before(Items.BONE, 144)
				.before(Items.SNOWBALL, 1)
				.before(Items.FILLED_MAP, 1_472)
				.before(ItemTags.LOGS, 32)
				.before(ItemTags.PLANKS, 8)
				.before(ItemTags.SAPLINGS, 32)
				.before(Tags.Items.RODS_WOODEN, 4)
				.before(ItemTags.LEAVES, 1)
				.before(Items.MANGROVE_ROOTS, 4)
				.before(ItemTags.WOOL, 48)
				.before(Items.NETHERITE_SCRAP, 12_288)
				.before(Tags.Items.GEMS_DIAMOND, 8_192)
				.before(Tags.Items.DUSTS_REDSTONE, 64)
				.before(Tags.Items.DUSTS_GLOWSTONE, 384);
		ConversionGroupBuilder shulkerGroupBuilder = defaultBuilder.group("shulker_box_recoloring")
				.comment("Propagate shulker box values to colored variants.");
		for (DyeColor color : DyeColor.values()) {
			shulkerGroupBuilder.conversion(ShulkerBoxBlock.getBlockByColor(color))
					.ingredient(Blocks.SHULKER_BOX)
					.ingredient(color.getTag())
					.end();
		}
	}

	private ItemStack horn(HolderLookup.Provider registries, ResourceKey<Instrument> instrument) {
		return registries.lookup(Registries.INSTRUMENT)
				.flatMap(instruments -> instruments.get(instrument))
				.map(inst -> InstrumentItem.create(Items.GOAT_HORN, inst))
				.orElseThrow(() -> new RuntimeException("Unable to find instrument for creating horn"));
	}

	private static NormalizedSimpleStack ingotTag(String ingot) {
		return tag("forge:ingots/" + ingot);
	}

	private static NormalizedSimpleStack gemTag(String gem) {
		return tag("forge:gems/" + gem);
	}

	private static NormalizedSimpleStack tag(String tag) {
		return NSSItem.createTag(new ResourceLocation(tag));
	}
}