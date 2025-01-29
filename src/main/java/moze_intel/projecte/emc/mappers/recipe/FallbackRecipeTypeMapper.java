package moze_intel.projecte.emc.mappers.recipe;

import java.util.Collection;
import java.util.List;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

@RecipeTypeMapper(priority = Integer.MIN_VALUE)
public class FallbackRecipeTypeMapper extends BaseRecipeTypeMapper {

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_FALLBACK.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_FALLBACK.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_FALLBACK.tooltip();
	}

	@Override
	public boolean canHandle(RecipeType<?> recipeType) {
		//Pretend that we can handle
		return true;
	}

	@Override
	public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
		Recipe<?> recipe = recipeHolder.value();
		if (recipe instanceof CraftingRecipe || recipe instanceof AbstractCookingRecipe || recipe instanceof SingleItemRecipe ||
			//Note: We may be able to do SmithingRecipe instead of checking these two subtypes, but we likely won't be able to retrieve the ingredients
			recipe instanceof SmithingTransformRecipe || recipe instanceof SmithingTrimRecipe) {
			return super.handleRecipe(mapper, recipeHolder, registryAccess, fakeGroupManager);
		}
		return false;
	}

	@Override
	protected Collection<Ingredient> getIngredients(Recipe<?> recipe) {
		Collection<Ingredient> ingredients = super.getIngredients(recipe);
		if (ingredients.isEmpty()) {
			//If the extension of upgrade recipe doesn't override getIngredients (just like vanilla doesn't)
			// grab the values from the recipe's object itself
			if (recipe instanceof SmithingTransformRecipe transformRecipe) {
				return List.of(transformRecipe.base, transformRecipe.addition, transformRecipe.template);
			} else if (recipe instanceof SmithingTrimRecipe trimRecipe) {
				return List.of(trimRecipe.base, trimRecipe.addition, trimRecipe.template);
			}
		}
		return ingredients;
	}
}