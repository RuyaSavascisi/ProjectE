package moze_intel.projecte.integration.jei.world_transmute;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import moze_intel.projecte.api.world_transmutation.IWorldTransmutation;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.world_transmutation.WorldTransmutationManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;
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
		if (recipe.hasInput()) {
			addIngredient(builder, RecipeIngredientRole.INPUT, 16, recipe.getInput());
		}
		int xPos = 96;
		for (Either<ItemStack, FluidStack> output : recipe.getOutput()) {
			addIngredient(builder, RecipeIngredientRole.OUTPUT, xPos, output);
			xPos += 16;
		}
	}

	private void addIngredient(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int xPos, Either<ItemStack, FluidStack> ingredient) {
		IRecipeSlotBuilder slot = builder.addSlot(role, xPos, 16);
		ingredient.ifLeft(slot::addItemStack);
		Optional<FluidStack> right = ingredient.right();
		//noinspection OptionalIsPresent - Capturing lambda
		if (right.isPresent()) {
			slot.addIngredient(NeoForgeTypes.FLUID_STACK, right.get())
					.setFluidRenderer(FluidType.BUCKET_VOLUME, false, 16, 16);
		}
	}

	@Override
	public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull WorldTransmuteEntry recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (mouseX > 67 && mouseX < 107 && mouseY > 18 && mouseY < 38) {
			tooltip.add(PELang.WORLD_TRANSMUTE_DESCRIPTION.translate());
		}
	}

	public static List<WorldTransmuteEntry> getAllTransmutations() {
		//All the ones that have a block state that can be rendered in JEI.
		//For example only render one pumpkin to melon transmutation
		Set<ItemStack> seenItems = new ObjectOpenCustomHashSet<>(ItemStackLinkedSet.TYPE_AND_TAG);
		Set<FluidStack> seenFluids = new ObjectOpenCustomHashSet<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS);
		List<WorldTransmuteEntry> visible = new ArrayList<>();
		for (IWorldTransmutation transmutation : WorldTransmutationManager.INSTANCE.getWorldTransmutations()) {
			WorldTransmuteEntry entry = new WorldTransmuteEntry(transmutation);
			if (entry.isRenderable() && entry.isUnseenInput(seenItems, seenFluids)) {
				//Only add recipes for transmutations we have not had
				visible.add(entry);
			}
		}
		return visible;
	}
}