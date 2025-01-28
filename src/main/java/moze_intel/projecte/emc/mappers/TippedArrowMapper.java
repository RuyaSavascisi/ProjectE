package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

@EMCMapper
public class TippedArrowMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		int recipeCount = 0;
		NSSItem nssArrow = NSSItem.createItem(Items.ARROW);
		for (Holder<Potion> potionType : BuiltInRegistries.POTION.holders().toList()) {
			mapper.addConversion(8, NSSItem.createItem(PotionContents.createItemStack(Items.TIPPED_ARROW, potionType)), EMCHelper.intMapOf(
					nssArrow, 8,
					NSSItem.createItem(PotionContents.createItemStack(Items.LINGERING_POTION, potionType)), 1
			));
			recipeCount++;
		}
		PECore.debugLog("TippedArrowMapper Statistics:");
		PECore.debugLog("Found {} Tipped Arrow Recipes", recipeCount);
	}

	@Override
	public String getName() {
		return "TippedArrowMapper";
	}

	@Override
	public String getDescription() {
		return "Add Conversions for all lingering potions to arrow recipes";
	}
}