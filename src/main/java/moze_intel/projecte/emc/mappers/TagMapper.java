package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Collections;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

public class TagMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, final CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		for (NSSTag stack : AbstractNSSTag.getAllCreatedTags()) {
			stack.forEachElement(mapper, stack, (collector, normalizedSimpleStack, tag) -> {
				//Tag -> element
				collector.addConversion(1, tag, Collections.singletonList(normalizedSimpleStack));
				//Element -> tag
				collector.addConversion(1, normalizedSimpleStack, Collections.singletonList(tag));
			});
		}
	}

	@Override
	public String getName() {
		return "TagMapper";
	}

	@Override
	public String getDescription() {
		return "Adds back and forth conversions of objects and their Tag variant. (EMC values assigned to tags will not behave properly if this mapper is disabled)";
	}
}