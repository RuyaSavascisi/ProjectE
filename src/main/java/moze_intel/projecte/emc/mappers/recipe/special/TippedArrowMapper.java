package moze_intel.projecte.emc.mappers.recipe.special;

import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.TippedArrowRecipe;

@RecipeTypeMapper
public class TippedArrowMapper extends SpecialRecipeMapper<TippedArrowRecipe> {

	@Override
	protected Class<TippedArrowRecipe> getRecipeClass() {
		return TippedArrowRecipe.class;
	}

	@Override
	protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
		NSSItem nssArrow = NSSItem.createItem(Items.ARROW);
		List<Reference<Potion>> potions = BuiltInRegistries.POTION.holders().toList();
		for (Holder<Potion> potionType : potions) {
			mapper.addConversion(8, NSSItem.createItem(PotionContents.createItemStack(Items.TIPPED_ARROW, potionType)), EMCHelper.intMapOf(
					nssArrow, 8,
					NSSItem.createItem(PotionContents.createItemStack(Items.LINGERING_POTION, potionType)), 1
			));
		}
		PECore.debugLog("{} Statistics:", getName());
		PECore.debugLog("Found {} Tipped Arrow Recipes", potions.size());
		return true;
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_TIPPED_ARROW.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_TIPPED_ARROW.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_TIPPED_ARROW.tooltip();
	}
}