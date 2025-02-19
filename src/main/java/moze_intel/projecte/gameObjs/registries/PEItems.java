package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.CataliticLens;
import moze_intel.projecte.gameObjs.items.DestructionCatalyst;
import moze_intel.projecte.gameObjs.items.DiviningRod;
import moze_intel.projecte.gameObjs.items.EvertideAmulet;
import moze_intel.projecte.gameObjs.items.GemEternalDensity;
import moze_intel.projecte.gameObjs.items.HyperkineticLens;
import moze_intel.projecte.gameObjs.items.KleinStar;
import moze_intel.projecte.gameObjs.items.KleinStar.KleinTier;
import moze_intel.projecte.gameObjs.items.MercurialEye;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.items.RepairTalisman;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.gameObjs.items.TransmutationTablet;
import moze_intel.projecte.gameObjs.items.VolcaniteAmulet;
import moze_intel.projecte.gameObjs.items.armor.DMArmor;
import moze_intel.projecte.gameObjs.items.armor.GemChest;
import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import moze_intel.projecte.gameObjs.items.armor.GemHelmet;
import moze_intel.projecte.gameObjs.items.armor.GemLegs;
import moze_intel.projecte.gameObjs.items.armor.RMArmor;
import moze_intel.projecte.gameObjs.items.rings.Arcana;
import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import moze_intel.projecte.gameObjs.items.rings.BlackHoleBand;
import moze_intel.projecte.gameObjs.items.rings.BodyStone;
import moze_intel.projecte.gameObjs.items.rings.HarvestGoddess;
import moze_intel.projecte.gameObjs.items.rings.Ignition;
import moze_intel.projecte.gameObjs.items.rings.LifeStone;
import moze_intel.projecte.gameObjs.items.rings.MindStone;
import moze_intel.projecte.gameObjs.items.rings.SWRG;
import moze_intel.projecte.gameObjs.items.rings.SoulStone;
import moze_intel.projecte.gameObjs.items.rings.TimeWatch;
import moze_intel.projecte.gameObjs.items.rings.VoidRing;
import moze_intel.projecte.gameObjs.items.rings.Zero;
import moze_intel.projecte.gameObjs.items.tools.PEAxe;
import moze_intel.projecte.gameObjs.items.tools.PEHammer;
import moze_intel.projecte.gameObjs.items.tools.PEHoe;
import moze_intel.projecte.gameObjs.items.tools.PEKatar;
import moze_intel.projecte.gameObjs.items.tools.PEMorningStar;
import moze_intel.projecte.gameObjs.items.tools.PEPickaxe;
import moze_intel.projecte.gameObjs.items.tools.PEShears;
import moze_intel.projecte.gameObjs.items.tools.PEShovel;
import moze_intel.projecte.gameObjs.items.tools.PESword;
import moze_intel.projecte.gameObjs.items.tools.RedMatterSword;
import moze_intel.projecte.gameObjs.registration.impl.ItemDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.ItemRegistryObject;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class PEItems {

	public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(PECore.MODID);

	public static final ItemRegistryObject<PhilosophersStone> PHILOSOPHERS_STONE = ITEMS.registerNoStack("philosophers_stone", PhilosophersStone::new);
	public static final ItemRegistryObject<RepairTalisman> REPAIR_TALISMAN = ITEMS.registerNoStack("repair_talisman", RepairTalisman::new);
	public static final ItemRegistryObject<Item> LOW_COVALENCE_DUST = ITEMS.register("low_covalence_dust");
	public static final ItemRegistryObject<Item> MEDIUM_COVALENCE_DUST = ITEMS.register("medium_covalence_dust");
	public static final ItemRegistryObject<Item> HIGH_COVALENCE_DUST = ITEMS.register("high_covalence_dust");

	public static final ItemRegistryObject<AlchemicalBag> WHITE_ALCHEMICAL_BAG = registerBag(DyeColor.WHITE);
	public static final ItemRegistryObject<AlchemicalBag> ORANGE_ALCHEMICAL_BAG = registerBag(DyeColor.ORANGE);
	public static final ItemRegistryObject<AlchemicalBag> MAGENTA_ALCHEMICAL_BAG = registerBag(DyeColor.MAGENTA);
	public static final ItemRegistryObject<AlchemicalBag> LIGHT_BLUE_ALCHEMICAL_BAG = registerBag(DyeColor.LIGHT_BLUE);
	public static final ItemRegistryObject<AlchemicalBag> YELLOW_ALCHEMICAL_BAG = registerBag(DyeColor.YELLOW);
	public static final ItemRegistryObject<AlchemicalBag> LIME_ALCHEMICAL_BAG = registerBag(DyeColor.LIME);
	public static final ItemRegistryObject<AlchemicalBag> PINK_ALCHEMICAL_BAG = registerBag(DyeColor.PINK);
	public static final ItemRegistryObject<AlchemicalBag> GRAY_ALCHEMICAL_BAG = registerBag(DyeColor.GRAY);
	public static final ItemRegistryObject<AlchemicalBag> LIGHT_GRAY_ALCHEMICAL_BAG = registerBag(DyeColor.LIGHT_GRAY);
	public static final ItemRegistryObject<AlchemicalBag> CYAN_ALCHEMICAL_BAG = registerBag(DyeColor.CYAN);
	public static final ItemRegistryObject<AlchemicalBag> PURPLE_ALCHEMICAL_BAG = registerBag(DyeColor.PURPLE);
	public static final ItemRegistryObject<AlchemicalBag> BLUE_ALCHEMICAL_BAG = registerBag(DyeColor.BLUE);
	public static final ItemRegistryObject<AlchemicalBag> BROWN_ALCHEMICAL_BAG = registerBag(DyeColor.BROWN);
	public static final ItemRegistryObject<AlchemicalBag> GREEN_ALCHEMICAL_BAG = registerBag(DyeColor.GREEN);
	public static final ItemRegistryObject<AlchemicalBag> RED_ALCHEMICAL_BAG = registerBag(DyeColor.RED);
	public static final ItemRegistryObject<AlchemicalBag> BLACK_ALCHEMICAL_BAG = registerBag(DyeColor.BLACK);

	public static final ItemRegistryObject<KleinStar> KLEIN_STAR_EIN = registerKleinStar(KleinTier.EIN);
	public static final ItemRegistryObject<KleinStar> KLEIN_STAR_ZWEI = registerKleinStar(KleinTier.ZWEI);
	public static final ItemRegistryObject<KleinStar> KLEIN_STAR_DREI = registerKleinStar(KleinTier.DREI);
	public static final ItemRegistryObject<KleinStar> KLEIN_STAR_VIER = registerKleinStar(KleinTier.VIER);
	public static final ItemRegistryObject<KleinStar> KLEIN_STAR_SPHERE = registerKleinStar(KleinTier.SPHERE);
	public static final ItemRegistryObject<KleinStar> KLEIN_STAR_OMEGA = registerKleinStar(KleinTier.OMEGA);

	public static final ItemRegistryObject<Item> ALCHEMICAL_COAL = ITEMS.register("alchemical_coal");
	public static final ItemRegistryObject<Item> MOBIUS_FUEL = ITEMS.register("mobius_fuel");
	public static final ItemRegistryObject<Item> AETERNALIS_FUEL = ITEMS.registerSimple("aeternalis_fuel", properties -> new Item(properties.rarity(Rarity.RARE)));
	public static final ItemRegistryObject<Item> DARK_MATTER = ITEMS.registerFireImmune("dark_matter");
	public static final ItemRegistryObject<Item> RED_MATTER = ITEMS.registerFireImmune("red_matter");

	public static final ItemRegistryObject<PEPickaxe> DARK_MATTER_PICKAXE = ITEMS.registerNoStackFireImmune("dm_pick", properties -> new PEPickaxe(EnumMatterType.DARK_MATTER, 2, properties));
	public static final ItemRegistryObject<PEAxe> DARK_MATTER_AXE = ITEMS.registerNoStackFireImmune("dm_axe", properties -> new PEAxe(EnumMatterType.DARK_MATTER, 2, properties));
	public static final ItemRegistryObject<PEShovel> DARK_MATTER_SHOVEL = ITEMS.registerNoStackFireImmune("dm_shovel", properties -> new PEShovel(EnumMatterType.DARK_MATTER, 2, properties));
	public static final ItemRegistryObject<PESword> DARK_MATTER_SWORD = ITEMS.registerNoStackFireImmune("dm_sword", properties -> new PESword(EnumMatterType.DARK_MATTER, 2, 9, properties));
	public static final ItemRegistryObject<PEHoe> DARK_MATTER_HOE = ITEMS.registerNoStackFireImmune("dm_hoe", properties -> new PEHoe(EnumMatterType.DARK_MATTER, 2, properties));
	public static final ItemRegistryObject<PEShears> DARK_MATTER_SHEARS = ITEMS.registerNoStackFireImmune("dm_shears", properties -> new PEShears(EnumMatterType.DARK_MATTER, 2, properties));
	public static final ItemRegistryObject<PEHammer> DARK_MATTER_HAMMER = ITEMS.registerNoStackFireImmune("dm_hammer", properties -> new PEHammer(EnumMatterType.DARK_MATTER, 2, properties));

	public static final ItemRegistryObject<PEPickaxe> RED_MATTER_PICKAXE = ITEMS.registerNoStackFireImmune("rm_pick", properties -> new PEPickaxe(EnumMatterType.RED_MATTER, 3, properties));
	public static final ItemRegistryObject<PEAxe> RED_MATTER_AXE = ITEMS.registerNoStackFireImmune("rm_axe", properties -> new PEAxe(EnumMatterType.RED_MATTER, 3, properties));
	public static final ItemRegistryObject<PEShovel> RED_MATTER_SHOVEL = ITEMS.registerNoStackFireImmune("rm_shovel", properties -> new PEShovel(EnumMatterType.RED_MATTER, 3, properties));
	public static final ItemRegistryObject<RedMatterSword> RED_MATTER_SWORD = ITEMS.registerNoStackFireImmune("rm_sword", RedMatterSword::new);
	public static final ItemRegistryObject<PEHoe> RED_MATTER_HOE = ITEMS.registerNoStackFireImmune("rm_hoe", properties -> new PEHoe(EnumMatterType.RED_MATTER, 3, properties));
	public static final ItemRegistryObject<PEShears> RED_MATTER_SHEARS = ITEMS.registerNoStackFireImmune("rm_shears", properties -> new PEShears(EnumMatterType.RED_MATTER, 3, properties));
	public static final ItemRegistryObject<PEHammer> RED_MATTER_HAMMER = ITEMS.registerNoStackFireImmune("rm_hammer", properties -> new PEHammer(EnumMatterType.RED_MATTER, 3, properties));
	public static final ItemRegistryObject<PEKatar> RED_MATTER_KATAR = ITEMS.registerNoStackFireImmune("rm_katar", properties -> new PEKatar(EnumMatterType.RED_MATTER, 4, properties));
	public static final ItemRegistryObject<PEMorningStar> RED_MATTER_MORNING_STAR = ITEMS.registerNoStackFireImmune("rm_morning_star", properties -> new PEMorningStar(EnumMatterType.RED_MATTER, 4, properties));

	public static final ItemRegistryObject<DMArmor> DARK_MATTER_HELMET = ITEMS.registerNoStackFireImmune("dm_helmet", properties -> new DMArmor(ArmorItem.Type.HELMET, properties));
	public static final ItemRegistryObject<DMArmor> DARK_MATTER_CHESTPLATE = ITEMS.registerNoStackFireImmune("dm_chestplate", properties -> new DMArmor(ArmorItem.Type.CHESTPLATE, properties));
	public static final ItemRegistryObject<DMArmor> DARK_MATTER_LEGGINGS = ITEMS.registerNoStackFireImmune("dm_leggings", properties -> new DMArmor(ArmorItem.Type.LEGGINGS, properties));
	public static final ItemRegistryObject<DMArmor> DARK_MATTER_BOOTS = ITEMS.registerNoStackFireImmune("dm_boots", properties -> new DMArmor(ArmorItem.Type.BOOTS, properties));

	public static final ItemRegistryObject<RMArmor> RED_MATTER_HELMET = ITEMS.registerNoStackFireImmune("rm_helmet", properties -> new RMArmor(ArmorItem.Type.HELMET, properties));
	public static final ItemRegistryObject<RMArmor> RED_MATTER_CHESTPLATE = ITEMS.registerNoStackFireImmune("rm_chestplate", properties -> new RMArmor(ArmorItem.Type.CHESTPLATE, properties));
	public static final ItemRegistryObject<RMArmor> RED_MATTER_LEGGINGS = ITEMS.registerNoStackFireImmune("rm_leggings", properties -> new RMArmor(ArmorItem.Type.LEGGINGS, properties));
	public static final ItemRegistryObject<RMArmor> RED_MATTER_BOOTS = ITEMS.registerNoStackFireImmune("rm_boots", properties -> new RMArmor(ArmorItem.Type.BOOTS, properties));

	public static final ItemRegistryObject<GemHelmet> GEM_HELMET = ITEMS.registerNoStackFireImmune("gem_helmet", GemHelmet::new);
	public static final ItemRegistryObject<GemChest> GEM_CHESTPLATE = ITEMS.registerNoStackFireImmune("gem_chestplate", GemChest::new);
	public static final ItemRegistryObject<GemLegs> GEM_LEGGINGS = ITEMS.registerNoStackFireImmune("gem_leggings", GemLegs::new);
	public static final ItemRegistryObject<GemFeet> GEM_BOOTS = ITEMS.registerNoStackFireImmune("gem_boots", GemFeet::new);

	public static final ItemRegistryObject<Item> IRON_BAND = ITEMS.register("iron_band");
	public static final ItemRegistryObject<BlackHoleBand> BLACK_HOLE_BAND = ITEMS.registerNoStackFireImmune("black_hole_band", BlackHoleBand::new);
	public static final ItemRegistryObject<ArchangelSmite> ARCHANGEL_SMITE = ITEMS.registerNoStackFireImmune("archangel_smite", ArchangelSmite::new);
	public static final ItemRegistryObject<HarvestGoddess> HARVEST_GODDESS_BAND = ITEMS.registerNoStackFireImmune("harvest_goddess_band", HarvestGoddess::new);
	public static final ItemRegistryObject<Ignition> IGNITION_RING = ITEMS.registerNoStackFireImmune("ignition_ring", Ignition::new);
	public static final ItemRegistryObject<Zero> ZERO_RING = ITEMS.registerNoStackFireImmune("zero_ring", Zero::new);
	public static final ItemRegistryObject<SWRG> SWIFTWOLF_RENDING_GALE = ITEMS.registerNoStackFireImmune("swiftwolf_rending_gale", SWRG::new);
	public static final ItemRegistryObject<TimeWatch> WATCH_OF_FLOWING_TIME = ITEMS.registerNoStackFireImmune("watch_of_flowing_time", TimeWatch::new);
	public static final ItemRegistryObject<EvertideAmulet> EVERTIDE_AMULET = ITEMS.registerNoStackFireImmune("evertide_amulet", EvertideAmulet::new);
	public static final ItemRegistryObject<VolcaniteAmulet> VOLCANITE_AMULET = ITEMS.registerNoStackFireImmune("volcanite_amulet", VolcaniteAmulet::new);
	public static final ItemRegistryObject<GemEternalDensity> GEM_OF_ETERNAL_DENSITY = ITEMS.registerNoStackFireImmune("gem_of_eternal_density", GemEternalDensity::new);
	public static final ItemRegistryObject<MercurialEye> MERCURIAL_EYE = ITEMS.registerNoStackFireImmune("mercurial_eye", MercurialEye::new);
	public static final ItemRegistryObject<VoidRing> VOID_RING = ITEMS.registerNoStackFireImmune("void_ring", VoidRing::new);
	public static final ItemRegistryObject<Arcana> ARCANA_RING = ITEMS.registerNoStackFireImmune("arcana_ring", properties -> new Arcana(properties.rarity(Rarity.RARE)));
	public static final ItemRegistryObject<BodyStone> BODY_STONE = ITEMS.registerNoStackFireImmune("body_stone", BodyStone::new);
	public static final ItemRegistryObject<SoulStone> SOUL_STONE = ITEMS.registerNoStackFireImmune("soul_stone", SoulStone::new);
	public static final ItemRegistryObject<MindStone> MIND_STONE = ITEMS.registerNoStackFireImmune("mind_stone", MindStone::new);
	public static final ItemRegistryObject<LifeStone> LIFE_STONE = ITEMS.registerNoStackFireImmune("life_stone", LifeStone::new);

	public static final ItemRegistryObject<DiviningRod> LOW_DIVINING_ROD = ITEMS.registerNoStack("divining_rod_1", properties -> new DiviningRod(properties, 1));
	public static final ItemRegistryObject<DiviningRod> MEDIUM_DIVINING_ROD = ITEMS.registerNoStack("divining_rod_2", properties -> new DiviningRod(properties, 2));
	public static final ItemRegistryObject<DiviningRod> HIGH_DIVINING_ROD = ITEMS.registerNoStack("divining_rod_3", properties -> new DiviningRod(properties, 3));

	public static final ItemRegistryObject<DestructionCatalyst> DESTRUCTION_CATALYST = ITEMS.registerNoStack("destruction_catalyst", DestructionCatalyst::new);
	public static final ItemRegistryObject<HyperkineticLens> HYPERKINETIC_LENS = ITEMS.registerNoStackFireImmune("hyperkinetic_lens", HyperkineticLens::new);
	public static final ItemRegistryObject<CataliticLens> CATALYTIC_LENS = ITEMS.registerNoStackFireImmune("catalytic_lens", CataliticLens::new);

	public static final ItemRegistryObject<Tome> TOME_OF_KNOWLEDGE = ITEMS.registerNoStack("tome", properties -> new Tome(properties.rarity(Rarity.EPIC)));
	public static final ItemRegistryObject<TransmutationTablet> TRANSMUTATION_TABLET = ITEMS.registerNoStackFireImmune("transmutation_tablet", TransmutationTablet::new);

	private static ItemRegistryObject<AlchemicalBag> registerBag(DyeColor color) {
		return ITEMS.registerNoStack(color.getName() + "_alchemical_bag", properties -> new AlchemicalBag(properties, color));
	}

	private static ItemRegistryObject<KleinStar> registerKleinStar(KleinTier tier) {
		return ITEMS.registerNoStack("klein_star_" + tier.name, properties -> {
			if (tier == KleinTier.OMEGA) {
				properties = properties.rarity(Rarity.EPIC);
			}
			return new KleinStar(properties, tier);
		});
	}

	public static AlchemicalBag getBag(DyeColor color) {
		return getBagReference(color).value();
	}

	public static ItemRegistryObject<AlchemicalBag> getBagReference(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_ALCHEMICAL_BAG;
			case ORANGE -> ORANGE_ALCHEMICAL_BAG;
			case MAGENTA -> MAGENTA_ALCHEMICAL_BAG;
			case LIGHT_BLUE -> LIGHT_BLUE_ALCHEMICAL_BAG;
			case YELLOW -> YELLOW_ALCHEMICAL_BAG;
			case LIME -> LIME_ALCHEMICAL_BAG;
			case PINK -> PINK_ALCHEMICAL_BAG;
			case GRAY -> GRAY_ALCHEMICAL_BAG;
			case LIGHT_GRAY -> LIGHT_GRAY_ALCHEMICAL_BAG;
			case CYAN -> CYAN_ALCHEMICAL_BAG;
			case PURPLE -> PURPLE_ALCHEMICAL_BAG;
			case BLUE -> BLUE_ALCHEMICAL_BAG;
			case BROWN -> BROWN_ALCHEMICAL_BAG;
			case GREEN -> GREEN_ALCHEMICAL_BAG;
			case RED -> RED_ALCHEMICAL_BAG;
			case BLACK -> BLACK_ALCHEMICAL_BAG;
		};
	}

	public static ItemRegistryObject<KleinStar> getStar(KleinTier tier) {
		return switch (tier) {
			case EIN -> KLEIN_STAR_EIN;
			case ZWEI -> KLEIN_STAR_ZWEI;
			case DREI -> KLEIN_STAR_DREI;
			case VIER -> KLEIN_STAR_VIER;
			case SPHERE -> KLEIN_STAR_SPHERE;
			case OMEGA -> KLEIN_STAR_OMEGA;
		};
	}
}