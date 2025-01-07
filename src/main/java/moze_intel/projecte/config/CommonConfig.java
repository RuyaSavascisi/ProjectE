package moze_intel.projecte.config;

import moze_intel.projecte.config.value.CachedBooleanValue;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * For config options that either the server or the client may care about but do not have to agree upon.
 */
public class CommonConfig extends BasePEConfig {

	private final ModConfigSpec configSpec;

	public final CachedBooleanValue debugLogging;
	public final CachedBooleanValue craftableTome;
	public final CachedBooleanValue fullKleinStars;

	CommonConfig() {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		debugLogging = CachedBooleanValue.wrap(this, builder
				.comment("Enable more verbose debug logging")
				.define("debugLogging", false));
		craftableTome = CachedBooleanValue.wrap(this, builder
				.comment("The Tome of Knowledge can be crafted.")
				.define("craftableTome", false));
		fullKleinStars = CachedBooleanValue.wrap(this, builder
				.comment("Require full omega klein stars in the tome of knowledge and gem armor recipes. This is the same behavior that EE2 had.")
				.define("fullKleinStars", false));
		configSpec = builder.build();
	}

	@Override
	public String getFileName() {
		return "common";
	}

	@Override
	public ModConfigSpec getConfigSpec() {
		return configSpec;
	}

	@Override
	public ModConfig.Type getConfigType() {
		return ModConfig.Type.COMMON;
	}
}