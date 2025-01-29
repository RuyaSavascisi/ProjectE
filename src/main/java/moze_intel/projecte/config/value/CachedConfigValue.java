package moze_intel.projecte.config.value;

import moze_intel.projecte.config.IPEConfig;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class CachedConfigValue<T> extends CachedResolvableConfigValue<T, T> {

	protected CachedConfigValue(IPEConfig config, ConfigValue<T> internal) {
		super(config, internal);
	}

	public static <T> CachedConfigValue<T> wrap(IPEConfig config, ConfigValue<T> internal) {
		return new CachedConfigValue<>(config, internal);
	}

	@Override
	protected T resolve(T encoded) {
		return encoded;
	}

	@Override
	protected T encode(T value) {
		return value;
	}
}