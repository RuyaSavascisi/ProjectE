package moze_intel.projecte.gameObjs.customRecipes;

import moze_intel.projecte.gameObjs.items.KleinStar;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

public class RecipeShapelessKleinStar extends WrappedShapelessRecipe {

	public RecipeShapelessKleinStar(ShapelessRecipe internal) {
		super(internal);
	}

	@NotNull
	@Override
	public RecipeSerializer<?> getSerializer() {
		return PERecipeSerializers.KLEIN.get();
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingInput inv, @NotNull HolderLookup.Provider registryAccess) {
		ItemStack result = getInternal().assemble(inv, registryAccess);
		long storedEMC = 0;
		for (ItemStack stack : inv.items()) {
			if (!stack.isEmpty() && stack.getItem() instanceof KleinStar star) {
				storedEMC += star.getStoredEmc(stack);
			}
		}
		if (storedEMC != 0 && result.getItem() instanceof KleinStar) {
			result.set(PEDataComponentTypes.STORED_EMC, storedEMC);
		}
		return result;
	}

	@Override
	public boolean isSpecial() {
		//Allow the klein recipes to show up in the recipe book and in JEI
		return false;
	}
}