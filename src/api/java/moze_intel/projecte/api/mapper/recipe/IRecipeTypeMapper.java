package moze_intel.projecte.api.mapper.recipe;

import moze_intel.projecte.api.config.IConfigBuilder;
import moze_intel.projecte.api.config.IConfigurableElement;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Interface for Classes that want to make Contributions to the EMC Mapping via the CraftingMapper.
 */
public interface IRecipeTypeMapper extends IConfigurableElement {

	/**
	 * {@inheritDoc} If this returns {@code false} {@link #canHandle(RecipeType)} and
	 * {@link #handleRecipe(IMappingCollector, RecipeHolder, RegistryAccess, INSSFakeGroupManager)} will not be called.
	 */
	@Override
	default boolean isAvailable() {
		return IConfigurableElement.super.isAvailable();
	}

	/**
	 * Checks if this {@link IRecipeTypeMapper} can handle the given recipe type.
	 *
	 * @param recipeType The {@link RecipeType} to check.
	 *
	 * @return {@code true} if this {@link IRecipeTypeMapper} can handle the given {@link RecipeType}, {@code false} otherwise.
	 */
	boolean canHandle(RecipeType<?> recipeType);

	/**
	 * Attempts to handle a {@link Recipe} that is of a type restricted by {@link #canHandle(RecipeType)}.
	 *
	 * @param mapper           The mapper to add mapping data to.
	 * @param recipeHolder     The recipe to attempt to map.
	 * @param registryAccess   Registry access for use in getting recipe outputs and the like
	 * @param fakeGroupManager The manager for helping create and manage "groupings" of valid ingredients.
	 *
	 * @return {@code true} if the {@link IRecipeTypeMapper} handled the given {@link Recipe}, {@code false} otherwise
	 *
	 * @apiNote Make sure to call {@link #canHandle(RecipeType)} before calling this method.
	 * @implNote Due to how the fakeGroupManager works, {@link moze_intel.projecte.api.nss.NSSFake} implementations should only be created in this method with
	 * descriptions that are more complex than a single integer, as otherwise they may intersect with {@link NormalizedSimpleStack}s created by the fakeGroupManager.
	 */
	boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RecipeHolder<?> recipeHolder, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager);

	//TODO - 1.21: Docs, and update the docs for addMappings
	default void addConfigOptions(IConfigBuilder<IEMCMapper<NormalizedSimpleStack, Long>> configBuilder) {
	}
}