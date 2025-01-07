package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.Tags;

@EMCMapper
public class RawOreBlacklistMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		BuiltInRegistries.ITEM.getTag(Tags.Items.RAW_MATERIALS)
				.ifPresent(tag -> tag.stream()
						.map(holder -> NSSItem.createItem(holder.value()))
						.forEach(nssRawORe -> {
							mapper.setValueBefore(nssRawORe, 0L);
							mapper.setValueAfter(nssRawORe, 0L);
						}));
	}

	@Override
	public String getName() {
		return "RawOresBlacklistMapper";
	}

	@Override
	public String getDescription() {
		return "Set EMC=0 for everything in the c:raw_materials tag";
	}
}