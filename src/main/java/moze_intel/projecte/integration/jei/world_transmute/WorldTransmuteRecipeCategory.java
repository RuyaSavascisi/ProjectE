package moze_intel.projecte.integration.jei.world_transmute;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.imc.WorldTransmutationEntry;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.WorldTransmutations;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class WorldTransmuteRecipeCategory implements IRecipeCategory<WorldTransmuteEntry> {

	public static final RecipeType<WorldTransmuteEntry> RECIPE_TYPE = new RecipeType<>(PECore.rl("world_transmutation"), WorldTransmuteEntry.class);
	private final IDrawable arrow;
	private final IDrawable icon;

	public WorldTransmuteRecipeCategory(IGuiHelper guiHelper) {
		arrow = guiHelper.drawableBuilder(PECore.rl("textures/gui/arrow.png"), 0, 0, 22, 15).setTextureSize(32, 32).build();
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, PEItems.PHILOSOPHERS_STONE.asStack());
	}

	@NotNull
	@Override
	public RecipeType<WorldTransmuteEntry> getRecipeType() {
		return RECIPE_TYPE;
	}

	@NotNull
	@Override
	public Component getTitle() {
		return PELang.WORLD_TRANSMUTE.translate();
	}

	@Override
	public int getWidth() {
		return 135;
	}

	@Override
	public int getHeight() {
		return 48;
	}

	@NotNull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void draw(@NotNull WorldTransmuteEntry recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
		arrow.draw(graphics, 55, 18);
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull WorldTransmuteEntry recipe, @NotNull IFocusGroup focuses) {
		recipe.getInput().ifPresent(recipeInput ->
				recipeInput.ifLeft(input -> builder.addSlot(RecipeIngredientRole.INPUT, 16, 16)
						.addItemStack(input)
				).ifRight(input -> builder.addSlot(RecipeIngredientRole.INPUT, 16, 16)
						.addIngredient(NeoForgeTypes.FLUID_STACK, input)
						.setFluidRenderer(FluidType.BUCKET_VOLUME, false, 16, 16)
				)
		);
		int xPos = 96;
		for (Either<ItemStack, FluidStack> output : recipe.getOutput()) {
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, xPos, 16);
			output.ifLeft(slot::addItemStack)
					.ifRight(input -> slot
							.addIngredient(NeoForgeTypes.FLUID_STACK, input)
							.setFluidRenderer(FluidType.BUCKET_VOLUME, false, 16, 16)
					);
			xPos += 16;
		}
	}

	@Override
	public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull WorldTransmuteEntry recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (mouseX > 67 && mouseX < 107 && mouseY > 18 && mouseY < 38) {
			tooltip.add(PELang.WORLD_TRANSMUTE_DESCRIPTION.translate());
		}
	}

	public static List<WorldTransmuteEntry> getAllTransmutations() {
		List<WorldTransmutationEntry> allWorldTransmutations = WorldTransmutations.getWorldTransmutations();
		//All the ones that have a block state that can be rendered in JEI.
		//For example only render one pumpkin to melon transmutation
		List<WorldTransmuteEntry> visible = new ArrayList<>();
		allWorldTransmutations.forEach(entry -> {
			WorldTransmuteEntry e = new WorldTransmuteEntry(entry);
			if (e.isRenderable()) {
				boolean alreadyHas;
				FluidStack inputFluid = e.getInputFluid();
				if (inputFluid.isEmpty()) {
					ItemStack inputItem = e.getInputItem();
					alreadyHas = visible.stream().map(WorldTransmuteEntry::getInputItem).anyMatch(otherInputItem -> !otherInputItem.isEmpty() && ItemStack.isSameItemSameComponents(inputItem, otherInputItem));
				} else {
					alreadyHas = visible.stream().map(WorldTransmuteEntry::getInputFluid).anyMatch(otherInputFluid -> !otherInputFluid.isEmpty() && FluidStack.isSameFluidSameComponents(inputFluid, otherInputFluid));
				}
				if (!alreadyHas) {
					//Only add items that we haven't already had.
					visible.add(e);
				}
			}
		});
		return visible;
	}
}