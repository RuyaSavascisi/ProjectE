package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Optional;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags.Items;

@EMCMapper
public class RawOreBlacklistMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@EMCMapper.Instance
	public static final RawOreBlacklistMapper INSTANCE = new RawOreBlacklistMapper();

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(Items.RAW_MATERIALS);
		if (tag.isPresent()) {
			for (Holder<Item> holder : tag.get()) {
				NSSItem rawOre = NSSItem.createItem(holder);
				mapper.setValueBefore(rawOre, 0L);
				mapper.setValueAfter(rawOre, 0L);
			}
		}
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