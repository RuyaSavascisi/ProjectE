package moze_intel.projecte.config;

import moze_intel.projecte.PECore;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.21: Re-evaluate all these translations as well as how they look in the config gui
public enum PEConfigTranslations implements IConfigTranslation {
	//Client Config
	CLIENT_PHILO_OVERLAY("client.philo_overlay", "Pulsating Overlay", "The Philosopher's Stone overlay softly pulsates."),

	CLIENT_TOOLTIPS("client.tooltips", "Tooltip Settings", "Settings for configuring Tooltips provided by ProjectE.", true),

	//TODO - 1.21: Do we want a section for emc tooltips and then enabled, and require holding shift?
	CLIENT_TOOLTIPS_EMC("client.tooltips.emc", "EMC Tooltips", "Show the EMC value as a tooltip on items and blocks."),
	CLIENT_TOOLTIPS_EMC_SHIFT("client.tooltips.emc.shift", "Shift EMC Tooltips",
			"Requires holding shift to display the EMC value as a tooltip on items and blocks. Note: this does nothing if EMC Tooltips are disabled."),
	CLIENT_TOOLTIPS_LEARNED_SHIFT("client.tooltips.learned.shift", "Shift Learned Tooltips",
			"Requires holding shift to display the learned/unlearned text as a tooltip on items and blocks. Note: this does nothing if EMC Tooltips are disabled."),
	CLIENT_TOOLTIPS_PEDESTAL("client.tooltips.pedestal", "DM Pedestal Tooltips", "Show Dark Matter Pedestal functions in item tooltips."),
	CLIENT_TOOLTIPS_STATS("client.tooltips.stats", "Stat Tooltips", "Show stats as tooltips for various ProjectE blocks."),
	CLIENT_TOOLTIPS_TAGS("client.tooltips.tags", "Tag Tooltips", "Show item tags in tooltips (useful for custom EMC registration)."),

	//Common Config
	COMMON_DEBUG_LOGGING("common.debug_logging", "Debug Logging", "Enable more verbose debug logging."),

	COMMON_CRAFTING("common.crafting", "Crafting Settings", "Settings for configuring crafting requirements of specific ProjectE recipes.", true),
	COMMON_CRAFTING_TOME("common.crafting.tome", "Craftable Tome", "Enable crafting the Tome of Knowledge."),
	COMMON_CRAFTING_FULL_KLEIN("common.crafting.full_klein", "Require Full Klein Stars",
			"Require full omega klein stars in the tome of knowledge and gem armor recipes. This is the same behavior that EE2 had."),

	//Server Config
	SERVER_COOLDOWN("server.cooldown", "Cooldown Settings",
			"Settings for configuring the Cooldown (in ticks) for various features in ProjectE. "
			+ "A cooldown of -1 will disable the functionality. A cooldown of 0 will allow the actions to happen every tick. "
			+ "Use caution as a very low value on features that run automatically could cause TPS issues.", true),

	SERVER_COOLDOWN_PEDESTAL("server.cooldown.pedestal", "Pedestal Cooldown Settings",
			"Cooldown settings for various items within Dark Matter Pedestals.", true),
	SERVER_COOLDOWN_PEDESTAL_ARCHANGEL("server.cooldown.pedestal.archangel", "Archangel",
			"Delay between Archangel Smite shooting arrows while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_BODY_STONE("server.cooldown.pedestal.body_stone", "Body Stone",
			"Delay between Body Stone healing 0.5 shanks while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_EVERTIDE("server.cooldown.pedestal.evertide", "Evertide Amulet",
			"Delay between Evertide Amulet trying to start rain while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_HARVEST("server.cooldown.pedestal.harvest", "Harvest",
			"Delay between Harvest Goddess trying to grow and harvest while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_IGNITION("server.cooldown.pedestal.ignition", "Ignition",
			"Delay between Ignition Ring trying to light entities on fire while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_LIFE_STONE("server.cooldown.pedestal.life_stone", "Life Stone",
			"Delay between Life Stone healing both food and hunger by 0.5 shank/heart while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_REPAIR("server.cooldown.pedestal.repair", "Repair",
			"Delay between Talisman of Repair trying to repair player items while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_SWRG("server.cooldown.pedestal.swrg", "SWRG",
			"Delay between SWRG trying to smite mobs while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_SOUL_STONE("server.cooldown.pedestal.soul_stone", "Soul Stone",
			"Delay between Soul Stone healing 0.5 hearts while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_VOLCANITE("server.cooldown.pedestal.volcanite", "Volcanite Amulet",
			"Delay between Volcanite Amulet trying to stop rain while in the pedestal."),
	SERVER_COOLDOWN_PEDESTAL_ZERO("server.cooldown.pedestal.zero", "SWRG",
			"Delay between Zero Ring trying to extinguish entities and freezing ground while in the pedestal."),

	SERVER_COOLDOWN_PLAYER("server.cooldown.player", "Player Cooldown Settings", "Cooldown settings for various items when being used by a player.", true),
	SERVER_COOLDOWN_PLAYER_PROJECTILE("server.cooldown.player.projectile", "Projectile", "A cooldown for firing projectiles."),
	SERVER_COOLDOWN_PLAYER_GEM_CHESTPLATE("server.cooldown.player.gem_chestplate", "Gem Chestplate", "A cooldown for Gem Chestplate explosion."),
	SERVER_COOLDOWN_PLAYER_REPAIR("server.cooldown.player.repair", "Repair",
			"Delay between Talisman of Repair trying to repair player items while in a player's inventory."),
	SERVER_COOLDOWN_PLAYER_HEAL("server.cooldown.player.heal", "Heal",
			"Delay between heal attempts while in a player's inventory. (Soul Stone, Life Stone, Gem Helmet)."),
	SERVER_COOLDOWN_PLAYER_FEED("server.cooldown.player.feed", "Feed",
			"Delay between feed attempts while in a player's inventory. (Body Stone, Life Stone, Gem Helmet)."),


	SERVER_DIFFICULTY("server.difficulty", "Difficulty Settings", "Settings for configuring Difficulty options provided by ProjectE.", true),
	SERVER_DIFFICULTY_OFFENSIVE_ABILITIES("server.difficulty.offensive_abilities", "Offensive Abilities",
			"Set to false to disable Gem Armor offensive abilities (helmet zap and chestplate explosion)."),
	//TODO - 1.21: Reword this to not have to reference the default key
	SERVER_DIFFICULTY_KATAR_DEATH_AURA("server.difficulty.katar_death_aura", "Katar Death Aura", "Amount of damage Katar 'C' key deals."),

	SERVER_DIFFICULTY_COVALENCE_LOSS("server.difficulty.covalence_loss", "Covalence Loss",
			"Adjusting this ratio changes how much EMC is received when burning a item. For example setting this to 0.5 will return half of the EMC cost."),
	SERVER_DIFFICULTY_COVALENCE_LOSS_ROUNDING("server.difficulty.covalence_loss.rounding", "Covalence Loss Rounding",
			"How rounding occurs when Covalence Loss results in a burn value less than 1 EMC. If true the value will be rounded up to 1. "
			+ "If false the value will be rounded down to 0."),

	SERVER_EFFECTS("server.effects", "Effect Settings", "Settings for configuring Effect options provided by ProjectE.", true),
	SERVER_EFFECTS_TIME_PEDESTAL_BONUS("server.effects.time_pedestal.bonus", "Time Pedestal Bonus",
			"Bonus ticks given by the Watch of Flowing Time while in the pedestal. 0 = effectively no bonus."),
	SERVER_EFFECTS_TIME_PEDESTAL_MOB_SLOWNESS("server.effects.time_pedestal.mob_slowness", "Time Pedestal Mob Slowness",
			"Factor the Watch of Flowing Time slows down mobs by while in the pedestal. Set to 1.0 for no slowdown."),
	SERVER_EFFECTS_INTERDICTION_MODE("server.effects.interdiction_mode", "Interdiction Mode",
			"If true the Interdiction Torch only affects hostile mobs and projectiles. If false it affects all non blacklisted living entities."),

	SERVER_ITEMS("server.items", "Item Settings", "Settings for configuring Item options provided by ProjectE.", true),
	SERVER_ITEMS_PICKAXE_AOE_VEIN_MINING("server.items.pickaxe_aoe_vein_mining", "Pickaxe AOE Vein Mining",
			"Instead of vein mining the ore you right click with your Dark/Red Matter Pick/Star it vein mines all ores in an AOE around you "
			+ "like it did in ProjectE before version 1.4.4."),
	SERVER_ITEMS_HARVEST_BAND_GRASS("server.items.harvest_band_grass", "Harvest Band Grass",
			"Allows the Harvest Goddess Band to passively grow tall grass, flowers, etc, on top of grass blocks."),
	SERVER_ITEMS_DISABLE_ALL_RADIUS_MINING("server.items.disable_all_radius_mining", "Disable All Radius Mining",
			"If set to true, disables all radius-based mining functionality (right click of tools)."),
	SERVER_ITEMS_TIME_WATCH("server.items.time_watch", "Watch of Flowing Time", "Enables the Watch of Flowing Time."),
	SERVER_ITEMS_OP_EVERTIDE("server.items.op_evertide", "Overpowered Evertide Amulet",
			"Allow the Evertide amulet to place water in dimensions that water evaporates. For example: The Nether."),

	SERVER_MISC("server.misc", "Misc Settings", "Settings for configuring misc options provided by ProjectE.", true),
	SERVER_MISC_UNSAFE_KEY_BINDS("server.misc.unsafe_key_binds", "Unsafe Key Binds",
			"False requires your hand be empty for Gem Armor Offensive Abilities to be readied or triggered."),
	SERVER_MISC_LOOKING_AT_DISPLAY("server.misc.looking_at_display", "Looking At Display",
			"Shows the EMC value of blocks when looking at them in Jade, TOP, or WTHIT."),

	//Data Component Processors
	DCP_ARMOR_TRIM("processing.data_component_processor.armor_trim", "Armor Trim Processor", "Calculates EMC value of trimmed armor.", true),
	DCP_DAMAGE("processing.data_component_processor.damage", "Damage Processor", "Reduces the EMC value the more damaged an item is.", true),
	DCP_DECORATED_POT("processing.data_component_processor.decorated_pot", "Decorated Pot Processor",
			"Takes the different sherds into account for each decorated pot.", true),
	DCP_ENCHANTMENT("processing.data_component_processor.enchantment", "Enchantment Processor",
			"Increases the EMC value to take into account any enchantments on an item.", true),
	DCP_STORED_EMC("processing.data_component_processor.stored_emc", "Stored EMC Processor",
			"Increases the EMC value of the item to take into account any EMC the item has stored.", true),

	DCP_ENABLED("processing.enabled", "Enabled", "Determines whether this Data Component Processor is enabled and can adjust the EMC value of items."),
	DCP_PERSISTENT("processing.persistent", "Persistent",
			"Determines whether this Data Component Processor can affect the persistent data that gets saved to knowledge/copied in a condenser."),

	;

	private final String key;
	private final String title;
	private final String tooltip;
	@Nullable
	private final String button;

	PEConfigTranslations(String path, String title, String tooltip) {
		this(path, title, tooltip, false);
	}

	PEConfigTranslations(String path, String title, String tooltip, boolean isSection) {
		this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection));
	}

	PEConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
		this.key = Util.makeDescriptionId("configuration", PECore.rl(path));
		this.title = title;
		this.tooltip = tooltip;
		this.button = button;
	}

	@NotNull
	@Override
	public String getTranslationKey() {
		return key;
	}

	@Override
	public String title() {
		return title;
	}

	@Override
	public String tooltip() {
		return tooltip;
	}

	@Nullable
	@Override
	public String button() {
		return button;
	}
}