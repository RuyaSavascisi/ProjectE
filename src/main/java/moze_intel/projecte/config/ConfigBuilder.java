package moze_intel.projecte.config;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import moze_intel.projecte.api.config.IConfigBuilder;
import moze_intel.projecte.config.value.CachedBooleanValue;
import moze_intel.projecte.config.value.CachedEnumValue;
import moze_intel.projecte.config.value.CachedIntValue;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

public record ConfigBuilder<OBJ>(IPEConfig config, ModConfigSpec.Builder builder, OBJ object) implements IConfigBuilder<OBJ> {

	@Override
	public BooleanSupplier create(String path, boolean defaultValue) {
		return CachedBooleanValue.wrap(config, builder.define(path, defaultValue));
	}

	@Override
	public IntSupplier create(String path, int defaultValue) {
		return CachedIntValue.wrap(config, builder.define(path, defaultValue));
	}

	@Override
	public IntSupplier create(String path, int defaultValue, int minValue, int maxValue) {
		return CachedIntValue.wrap(config, builder.defineInRange(path, defaultValue, minValue, maxValue));
	}

	@Override
	public <T extends Enum<T> & TranslatableEnum> Supplier<T> create(String path, T defaultValue) {
		return CachedEnumValue.wrap(config, builder.defineEnum(path, defaultValue));
	}
}