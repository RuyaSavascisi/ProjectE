package moze_intel.projecte.emc.mappers;

import java.util.List;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

public class TagMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		for (NSSTag stack : AbstractNSSTag.getAllCreatedTags()) {
			stack.forEachElement(mapper, stack, (collector, normalizedSimpleStack, tag) -> {
				//Tag -> element
				collector.addConversion(1, tag, List.of(normalizedSimpleStack));
				//Element -> tag
				collector.addConversion(1, normalizedSimpleStack, List.of(tag));
			});
		}
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_TAG_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_TAG_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_TAG_MAPPER.tooltip();
	}
}