package moze_intel.projecte.emc.mappers.customConversions;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.conversion.ConversionGroup;
import moze_intel.projecte.api.conversion.CustomConversion;
import moze_intel.projecte.api.conversion.CustomConversionFile;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.impl.codec.PECodecHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.util.TriConsumer;

@EMCMapper
public class CustomConversionMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	private static final FileToIdConverter CONVERSION_LISTER = FileToIdConverter.json("pe_custom_conversions");
	private static final TriConsumer<IMappingCollector<NormalizedSimpleStack, Long>, NormalizedSimpleStack, CustomConversion> CONVERSION_CONSUMER =
			(collector, nss, conversion) ->
			collector.setValueFromConversion(conversion.count(), nss, conversion.ingredients());

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CUSTOM_CONVERSION_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CUSTOM_CONVERSION_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CUSTOM_CONVERSION_MAPPER.tooltip();
	}

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		Map<ResourceLocation, CustomConversionFile> files = load(registryAccess, resourceManager);
		for (CustomConversionFile file : files.values()) {
			addMappingsFromFile(file, mapper);
		}
	}

	private static Map<ResourceLocation, CustomConversionFile> load(RegistryAccess registryAccess, ResourceManager resourceManager) {
		Map<ResourceLocation, CustomConversionFile> loading = new HashMap<>();

		// Find all data/<domain>/pe_custom_conversions/foo/bar.json
		for (Map.Entry<ResourceLocation, List<Resource>> entry : CONVERSION_LISTER.listMatchingResourceStacks(resourceManager).entrySet()) {
			ResourceLocation file = entry.getKey();//<domain>:foo/bar
			ResourceLocation conversionId = CONVERSION_LISTER.fileToId(file);

			PECore.LOGGER.info("Considering file {}, ID {}", file, conversionId);
			NSSFake.setCurrentNamespace(conversionId.toString());

			// Iterate through all copies of this conversion, from lowest to highest priority datapack, merging the results together
			for (Resource resource : entry.getValue()) {
				try (Reader reader = resource.openAsReader()) {
					Optional<CustomConversionFile> fileOptional = PECodecHelper.read(registryAccess, reader, CustomConversionFile.CODEC, "custom conversion file");
					//noinspection OptionalIsPresent - Capturing lambda
					if (fileOptional.isPresent()) {
						loading.merge(conversionId, fileOptional.get(), CustomConversionFile::merge);
					}
				} catch (IOException e) {
					PECore.LOGGER.error("Could not load resource {}", file, e);
				}
			}
		}
		NSSFake.resetNamespace();
		return loading;
	}

	private static void addMappingsFromFile(CustomConversionFile file, IMappingCollector<NormalizedSimpleStack, Long> mapper) {
		for (Map.Entry<String, ConversionGroup> entry : file.groups().entrySet()) {
			ConversionGroup group = entry.getValue();
			PECore.debugLog("Adding conversions from group '{}' with comment '{}'", entry.getKey(), group.comment());
			for (CustomConversion conversion : group.conversions()) {
				mapper.addConversion(conversion.count(), conversion.output(), conversion.ingredients());
			}
		}

		//Note: We set it for each of the values in the tag to make sure it is properly taken into account when calculating the individual EMC values
		for (Object2LongMap.Entry<NormalizedSimpleStack> entry : file.values().setValueBefore().object2LongEntrySet()) {
			entry.getKey().forSelfAndEachElement(mapper, entry.getLongValue(), IMappingCollector::setValueBefore);
		}

		//Note: We set it for each of the values in the tag to make sure it is properly taken into account when calculating the individual EMC values
		for (Object2LongMap.Entry<NormalizedSimpleStack> entry : file.values().setValueAfter().object2LongEntrySet()) {
			entry.getKey().forSelfAndEachElement(mapper, entry.getLongValue(), IMappingCollector::setValueAfter);
		}

		for (CustomConversion customConversion : file.values().conversions()) {
			if (customConversion.propagateTags()) {
				customConversion.output().forSelfAndEachElement(mapper, customConversion, CONVERSION_CONSUMER);
			} else {
				CONVERSION_CONSUMER.accept(mapper, customConversion.output(), customConversion);
			}
		}
	}
}