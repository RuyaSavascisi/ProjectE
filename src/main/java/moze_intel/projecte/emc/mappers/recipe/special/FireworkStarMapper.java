package moze_intel.projecte.emc.mappers.recipe.special;

import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

//@RecipeTypeMapper
public class FireworkStarMapper implements IRecipeTypeMapper {
	//public net.minecraft.world.item.crafting.FireworkStarRecipe SHAPE_BY_ITEM
	//public net.minecraft.world.item.crafting.FireworkStarRecipe SHAPE_INGREDIENT
	//public net.minecraft.world.item.crafting.FireworkStarRecipe TRAIL_INGREDIENT
	//public net.minecraft.world.item.crafting.FireworkStarRecipe TWINKLE_INGREDIENT
	//public net.minecraft.world.item.crafting.FireworkStarRecipe GUNPOWDER_INGREDIENT

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_FIREWORK_STAR.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_FIREWORK_STAR.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_FIREWORK_STAR.tooltip();
	}

	@Override
	public boolean canHandle(RecipeType<?> recipeType) {
		return recipeType == RecipeType.CRAFTING;
	}

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess,
			INSSFakeGroupManager fakeGroupManager) {
		if (recipeHolder.value() instanceof FireworkStarRecipe) {
			//TODO - 1.21: Handle
			return true;
		} /*else if (recipeHolder.value() instanceof FireworkStarFadeRecipe recipe) {
			//TODO - 1.21: Handle
			return true;
		}*/
		return false;
	}
}