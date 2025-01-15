package moze_intel.projecte.integration.crafttweaker.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

@EMCMapper(requiredMods = "crafttweaker")
public class CrTConversionEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	private static final List<CrTConversion> storedConversions = new ArrayList<>();

	public static void addConversion(@NotNull CrTConversion conversion) {
		storedConversions.add(conversion);
	}

	public static void removeConversion(@NotNull CrTConversion conversion) {
		storedConversions.remove(conversion);
	}

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		for (CrTConversion apiConversion : storedConversions) {
			TriConsumer<IMappingCollector<NormalizedSimpleStack, Long>, NormalizedSimpleStack, CrTConversion> consumer;
			if (apiConversion.set) {
				consumer = (collector, nss, conversion) ->
						collector.setValueFromConversion(conversion.amount, nss, conversion.ingredients);
			} else {
				consumer = (collector, nss, conversion) ->
						collector.addConversion(conversion.amount, nss, conversion.ingredients);
			}
			if (apiConversion.propagateTags) {
				apiConversion.output.forSelfAndEachElement(mapper, apiConversion, consumer);
			} else {
				consumer.accept(mapper, apiConversion.output, apiConversion);
			}
			PECore.debugLog("CraftTweaker adding conversion for {}", apiConversion.output);
		}
	}

	@Override
	public String getName() {
		return "CrTConversionEMCMapper";
	}

	@Override
	public String getDescription() {
		return "Allows adding custom conversions through CraftTweaker. This behaves similarly to if someone used a custom conversion file instead.";
	}

	public record CrTConversion(NormalizedSimpleStack output, int amount, boolean propagateTags, boolean set, Map<NormalizedSimpleStack, Integer> ingredients) {}
}