package moze_intel.projecte.gameObjs.customRecipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class PhiloStoneSmeltingRecipe extends CustomRecipe {

	public PhiloStoneSmeltingRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(@NotNull CraftingInput inv, @NotNull Level level) {
		//If we have at least one matching recipe, return that we found a match
		return !getMatchingRecipes(inv, level).isEmpty();
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingInput inv, @NotNull HolderLookup.Provider registryAccess) {
		Set<RecipeHolder<SmeltingRecipe>> matchingRecipes = getMatchingRecipes(inv, ServerLifecycleHooks.getCurrentServer().overworld());
		if (matchingRecipes.isEmpty()) {
			return ItemStack.EMPTY;
		}
		//If we have at least one matching recipe, return the output
		//Note: It is multiplied by seven as we have seven inputs
		ItemStack output = matchingRecipes.stream().findFirst().get().value().getResultItem(registryAccess);
		return output.copyWithCount(output.getCount() * 7);
	}

	private Set<RecipeHolder<SmeltingRecipe>> getMatchingRecipes(CraftingInput inv, @NotNull Level level) {
		List<ItemStack> philoStones = new ArrayList<>();
		List<ItemStack> coals = new ArrayList<>();
		List<ItemStack> allItems = new ArrayList<>();
		for (ItemStack stack : inv.items()) {
			if (!stack.isEmpty()) {
				allItems.add(stack);
				if (allItems.size() > 9) {
					//Exit if we have more than 9 items total (for mods that may add larger crafting tables)
					return Collections.emptySet();
				}
				if (stack.is(PEItems.PHILOSOPHERS_STONE)) {
					philoStones.add(stack);
				}
				if (stack.is(ItemTags.COALS)) {
					coals.add(stack);
				}
			}
		}
		if (allItems.size() == 9) {
			//If we have exactly 9 items check for a matching recipe
			for (ItemStack philoStone : philoStones) {
				for (ItemStack coal : coals) {
					//Skip if the philosopher's stone is the same stack as the coal stack
					// This may be the case if a pack dev added the philosopher's stone to the coals tag
					if (philoStone != coal) {
						Set<RecipeHolder<SmeltingRecipe>> matchingRecipes = new HashSet<>();
						for (ItemStack stack : allItems) {
							//Ignore checking the piece of coal and the philosopher's stone
							if (stack != philoStone && stack != coal) {
								//And check all the other elements to find any matching recipes
								SingleRecipeInput furnaceInput = new SingleRecipeInput(stack);
								if (matchingRecipes.isEmpty()) {
									//If there are no matching recipes yet see if there are any recipes that match the current stack and add them if they are,
									// if we didn't end up adding any elements that means there are no matching recipes so fail
									if (!matchingRecipes.addAll(level.getRecipeManager().getRecipesFor(RecipeType.SMELTING, furnaceInput, level))) {
										return Collections.emptySet();
									}
								} else {
									//noinspection Java8CollectionRemoveIf - Capturing lambda
									for (Iterator<RecipeHolder<SmeltingRecipe>> iterator = matchingRecipes.iterator(); iterator.hasNext(); ) {
										RecipeHolder<SmeltingRecipe> recipe = iterator.next();
										if (!recipe.value().matches(furnaceInput, level)) {
											iterator.remove();
										}
									}
									//Because we might have removed some recipes, check one more time if they are empty
									if (matchingRecipes.isEmpty()) {
										//If it is exit due to there being no match
										return Collections.emptySet();
									}
								}
							}
						}
						if (!matchingRecipes.isEmpty()) {
							//We have at least one matching recipe, so return the found recipes
							return matchingRecipes;
						}
					}
				}
			}
		}
		return Collections.emptySet();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 9;
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return PERecipeSerializers.PHILO_STONE_SMELTING.get();
	}
}