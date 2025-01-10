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
	private static final String ENABLED = "enabled";
	private static final String PERSISTENT = "persistent";
	private static final String MAIN_KEY = "processors";

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
		//TODO - 1.21: Re-evaluate this initial push
		builder.comment("This config is used to control which Data Component Processors get used, and which ones actually contribute to the persistent data that gets " +
						"saved to knowledge/copied in a condenser.",
				"To disable an Data Component Processor set the '" + ENABLED + "' option for it to false.",
				"To disable an Data Component Processor from contributing to the persistent data set the '" + PERSISTENT + "' option for it to false. Note: that if there is no " +
				PERSISTENT + "' config option, the Data Component Processor never has any persistent data.")
				.push(MAIN_KEY);
		for (IDataComponentProcessor processor : processors) {
			processorConfigs.put(processor.getName(), new ProcessorConfig(this, builder, processor));
		}
		builder.pop();
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
			PECore.LOGGER.warn("Processor Config: '{}' is missing from the config.", name);
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
	public ModConfigSpec getConfigSpec() {
		return configSpec;
	}

	@Override
	public Type getConfigType() {
		return Type.SERVER;
	}

	@Override
	public boolean addToContainer() {
		return false;
	}

	private static class ProcessorConfig {

		public final CachedBooleanValue enabled;
		@Nullable
		public final CachedBooleanValue persistent;

		private ProcessorConfig(IPEConfig config, ModConfigSpec.Builder builder, IDataComponentProcessor processor) {
			builder.comment(processor.getDescription()).push(processor.getName());
			enabled = CachedBooleanValue.wrap(config, builder.define(ENABLED, processor.isAvailable()));
			if (processor.hasPersistentComponents()) {
				persistent = CachedBooleanValue.wrap(config, builder.define(PERSISTENT, processor.usePersistentComponents()));
			} else {
				persistent = null;
			}
			builder.pop();
		}
	}
}