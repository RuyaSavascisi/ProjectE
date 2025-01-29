package moze_intel.projecte.api.config;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

//TODO - 1.21: Docs and add other defaults that might be useful
public interface IConfigBuilder<OBJ> {

	ModConfigSpec.Builder builder();

	OBJ object();

	BooleanSupplier create(String path, boolean defaultValue);

	IntSupplier create(String path, int defaultValue);

	IntSupplier create(String path, int defaultValue, int minValue, int maxValue);

	//Note: Ensure that we provide a nice translated name for any enum value based configs we have
	<T extends Enum<T> & TranslatableEnum> Supplier<T> create(String path, T defaultValue);
}