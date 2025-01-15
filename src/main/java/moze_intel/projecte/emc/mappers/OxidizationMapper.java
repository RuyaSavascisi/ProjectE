package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Collections;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;

@EMCMapper
public class OxidizationMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		Registry<Block> blocks = registryAccess.registryOrThrow(Registries.BLOCK);
		int recipeCount = 0;
		for (Map.Entry<ResourceKey<Block>, Oxidizable> entry : blocks.getDataMap(NeoForgeDataMaps.OXIDIZABLES).entrySet()) {
			//Add conversions both directions due to scraping
			Block block = blocks.get(entry.getKey());
			if (block != null) {
				NSSItem unweathered = NSSItem.createItem(block);
				NSSItem weathered = NSSItem.createItem(entry.getValue().nextOxidationStage());
				mapper.addConversion(1, weathered, Collections.singleton(unweathered));
				mapper.addConversion(1, unweathered, Collections.singleton(weathered));
				recipeCount += 2;
			}
		}
		PECore.debugLog("OxidizationMapper Statistics:");
		PECore.debugLog("Found {} Oxidizable Conversions", recipeCount);
	}

	@Override
	public String getName() {
		return "OxidizationMapper";
	}

	@Override
	public String getDescription() {
		return "Add Conversions for all oxidizable blocks";
	}
}