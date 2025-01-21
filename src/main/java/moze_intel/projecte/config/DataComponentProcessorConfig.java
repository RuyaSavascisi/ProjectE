package moze_intel.projecte.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.config.value.CachedBooleanValue;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * For config options having to do with Data Component Processors. Syncs from server to client.
 */
public class DataComponentProcessorConfig extends BasePEConfig {

	private static DataComponentProcessorConfig INSTANCE;

	/**
	 * If the config has not already been initialized setup a config the with given list of {@link IDataComponentProcessor}s and creates a dummy "server" config so that it will be
	 * synced by the {@link net.neoforged.fml.config.ConfigTracker} from server to client.
	 *
	 * @implNote We register the dummy config as being owned by our mod container, but we don't tell the mod container about the dummy config so that it does not
	 * overwrite our main server config.
	 */
	public static void setup(@NotNull List<IDataComponentProcessor> processors) {
		if (INSTANCE == null) {
			ProjectEConfig.registerConfig(PECore.MOD_CONTAINER, INSTANCE = new DataComponentProcessorConfig(processors));
		}
	}

	private final ModConfigSpec configSpec;
	private final Map<String, ProcessorConfig> processorConfigs = new HashMap<>();

	private DataComponentProcessorConfig(@NotNull List<IDataComponentProcessor> processors) {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		for (IDataComponentProcessor processor : processors) {
			processorConfigs.put(processor.getName(), new ProcessorConfig(this, builder, processor));
		}
		configSpec = builder.build();
	}

	/**
	 * @return True if the given {@link IDataComponentProcessor} is enabled.
	 */
	public static boolean isEnabled(IDataComponentProcessor processor) {
		if (INSTANCE == null) {
			return true;
		}
		String name = processor.getName();
		ProcessorConfig processorConfig = INSTANCE.processorConfigs.get(name);
		if (processorConfig == null) {
			PECore.LOGGER.warn("Processor Config: '{}' is missing from the config.", name);
			return false;
		}
		return processorConfig.enabled.get();
	}

	/**
	 * @return True if the given {@link IDataComponentProcessor} should contribute to the persistent data.
	 */
	public static boolean hasPersistent(IDataComponentProcessor processor) {
		if (INSTANCE == null) {
			return false;
		}
		String name = processor.getName();
		ProcessorConfig processorConfig = INSTANCE.processorConfigs.get(name);
		if (processorConfig == null) {
			PECore.LOGGER.warn("Persistent processor Config: '{}' is missing from the config.", name);
			return false;
		} else if (processorConfig.persistent == null) {
			if (processor.hasPersistentComponents()) {
				PECore.LOGGER.warn("Processor Config: '{}' has persistent Data Components but is missing the config option.", name);
			}
			return false;
		}
		return processorConfig.persistent.get();
	}

	@Override
	public String getFileName() {
		return "processing";
	}

	@Override
	public String getTranslation() {
		return "Data Component Processor Config";
	}

	@Override
	public ModConfigSpec getConfigSpec() {
		return configSpec;
	}

	@Override
	public Type getConfigType() {
		return Type.SERVER;
	}

	private static class ProcessorConfig {

		private final CachedBooleanValue enabled;
		@Nullable
		private final CachedBooleanValue persistent;

		private ProcessorConfig(IPEConfig config, ModConfigSpec.Builder builder, IDataComponentProcessor processor) {
			builder.comment(processor.getDescription())
					.translation(processor.getTranslationKey())
					.push(processor.getName().replace(' ', '-'));
			enabled = CachedBooleanValue.wrap(config, PEConfigTranslations.DCP_ENABLED.applyToBuilder(builder).define("enabled", processor.isAvailable()));
			if (processor.hasPersistentComponents()) {
				persistent = CachedBooleanValue.wrap(config, PEConfigTranslations.DCP_PERSISTENT.applyToBuilder(builder).define("persistent", processor.usePersistentComponents()));
			} else {
				persistent = null;
			}
			builder.pop();
		}
	}
}