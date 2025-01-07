package moze_intel.projecte.integration.jei.collectors;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CollectorRecipeCategory implements IRecipeCategory<FuelUpgradeRecipe> {

	public static final RecipeType<FuelUpgradeRecipe> RECIPE_TYPE = new RecipeType<>(PECore.rl("collector"), FuelUpgradeRecipe.class);
	private final IDrawable arrow;
	private final IDrawable icon;

	public CollectorRecipeCategory(IGuiHelper guiHelper) {
		arrow = guiHelper.drawableBuilder(PECore.rl("textures/gui/arrow.png"), 0, 0, 22, 15).setTextureSize(32, 32).build();
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PEBlocks.COLLECTOR));
	}

	@NotNull
	@Override
	public RecipeType<FuelUpgradeRecipe> getRecipeType() {
		return RECIPE_TYPE;
	}

	@NotNull
	@Override
	public Component getTitle() {
		return PELang.JEI_COLLECTOR.translate();
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
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull FuelUpgradeRecipe recipe, @NotNull IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 16, 16)
				.addItemStack(recipe.input());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 16)
				.addItemStack(recipe.output());
	}

	@Override
	public void draw(FuelUpgradeRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
		Component emc = PELang.EMC.translate(recipe.upgradeEMC());
		Font fontRenderer = Minecraft.getInstance().font;
		int stringWidth = fontRenderer.width(emc);
		graphics.drawString(fontRenderer, emc.getVisualOrderText(), (getWidth() - stringWidth) / 2F, 5, 0x808080, false);
		arrow.draw(graphics, 55, 18);
	}
}