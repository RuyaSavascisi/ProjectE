package moze_intel.projecte.config.value;

import moze_intel.projecte.config.IPEConfig;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.TranslatableEnum;

public class CachedEnumValue<T extends Enum<T>> extends CachedConfigValue<T> {

	private CachedEnumValue(IPEConfig config, EnumValue<T> internal) {
		super(config, internal);
	}

	//Note: Ensure that we provide a nice translated name for any enum value based configs we have
	public static <T extends Enum<T> & TranslatableEnum> CachedEnumValue<T> wrap(IPEConfig config, EnumValue<T> internal) {
		return new CachedEnumValue<>(config, internal);
	}
}