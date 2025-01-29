package moze_intel.projecte.emc.mappers.recipe;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.config.IConfigBuilder;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.AnnotationHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.ModConfigSpec;

@EMCMapper
public class CraftingMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	//Note: None of our defaults just directly support all recipe types, as mods may extend it for "random" things and have more input types required than just items
	// We also do this via annotations to allow for broader support for looping specific recipes and handling them
	private final Map<String, BooleanSupplier> enabledRecipeMappers = new HashMap<>();
	private final List<IRecipeTypeMapper> recipeMappers;

	public CraftingMapper() {
		//Load any recipe type mappers when instantiating the crafting mapper
		recipeMappers = AnnotationHelper.getRecipeTypeMappers();
	}

	private boolean isRecipeMapperEnabled(IRecipeTypeMapper mapper) {
		BooleanSupplier supplier = enabledRecipeMappers.get(mapper.getName());
		return supplier == null || supplier.getAsBoolean();
	}

	@Override
	public void addConfigOptions(IConfigBuilder<IEMCMapper<NormalizedSimpleStack, Long>> configBuilder) {
		ModConfigSpec.Builder builder = configBuilder.builder();
		for (IRecipeTypeMapper recipeMapper : recipeMappers) {
			//TODO - 1.21: Do we want to prepend this by recipe mapper or anything, or at least document that you shouldn't name a recipe mapper "enabled"
			builder.comment(recipeMapper.getDescription())
					.translation(recipeMapper.getTranslationKey())
					.push(recipeMapper.getConfigPath());
			PEConfigTranslations.MAPPING_RECIPE_TYPE_MAPPER_ENABLED.applyToBuilder(builder);
			enabledRecipeMappers.put(recipeMapper.getName(), configBuilder.create("enabled", recipeMapper.isAvailable()));
			recipeMapper.addConfigOptions(configBuilder);
			builder.pop();
		}
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		NSSFake.setCurrentNamespace("craftingMapper");
		Map<ResourceLocation, RecipeCountInfo> recipeCount = new HashMap<>();
		Set<ResourceLocation> canNotMap = new HashSet<>();
		RecipeManager recipeManager = serverResources.getRecipeManager();
		//Make a new fake group manager here instead of across the entire mapper so that we can reclaim the memory when we are done with this method
		NSSFakeGroupManager fakeGroupManager = new NSSFakeGroupManager();
		for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : BuiltInRegistries.RECIPE_TYPE.entrySet()) {
			ResourceLocation typeRegistryName = entry.getKey().location();
			RecipeType<?> recipeType = entry.getValue();
			boolean wasHandled = false;
			List<RecipeHolder<?>> recipes = null;
			List<RecipeHolder<?>> unhandled = new ArrayList<>();
			for (IRecipeTypeMapper recipeMapper : recipeMappers) {
				if (isRecipeMapperEnabled(recipeMapper)) {
					//If the sub mapper is enabled, use it
					if (recipeMapper.canHandle(recipeType)) {
						if (recipes == null) {
							//If we haven't already retrieved the recipes, do so
							//Note: The unchecked cast is needed as while the IDE doesn't have a warning without it,
							// it will not actually compile due to IRecipeType's generic only having to be of IRecipe<?>
							// so no information is stored about the type of inventory for the recipe
							recipes = recipeManager.getAllRecipesFor((RecipeType) recipeType);
						}
						int numHandled = 0;
						for (RecipeHolder<?> recipeHolder : recipes) {
							try {
								if (recipeMapper.handleRecipe(mapper, recipeHolder, registryAccess, fakeGroupManager)) {
									numHandled++;
								} else {
									unhandled.add(recipeHolder);
								}
							} catch (Exception e) {
								PECore.LOGGER.error(LogUtils.FATAL_MARKER, "A fatal error occurred while trying to map the recipe: {}", recipeHolder.id());
								throw e;
							}
						}
						if (numHandled > 0 || recipes.isEmpty()) {
							if (recipeCount.containsKey(typeRegistryName)) {
								recipeCount.get(typeRegistryName).setUnhandled(unhandled);
							} else {
								recipeCount.put(typeRegistryName, new RecipeCountInfo(recipes.size(), unhandled));
							}
							wasHandled = true;
							if (unhandled.isEmpty()) {
								//If we have no more recipes that were unhandled break out of mapping this recipe type
								break;
							} else {
								//Otherwise we replace our collection of known recipes with the unhandled ones and reset the list of unhandled recipes
								recipes = unhandled;
								unhandled = new ArrayList<>();
							}
						}
						//If we didn't actually handle/map any recipes, continue looking
					}
				}
			}
			if (!wasHandled) {
				//Note: We cannot just look at not unhandled is empty as then if none of the mappers even support the type
				// it will not be true. We also don't have any issues due to how we modify the unhandled
				canNotMap.add(typeRegistryName);
			}
		}
		PECore.debugLog("{} Statistics:", getName());
		for (Map.Entry<ResourceLocation, RecipeCountInfo> entry : recipeCount.entrySet()) {
			ResourceLocation typeRegistryName = entry.getKey();
			RecipeCountInfo countInfo = entry.getValue();
			int total = countInfo.getTotalRecipes();
			List<RecipeHolder<?>> unhandled = countInfo.getUnhandled();
			PECore.debugLog("Found and handled {} of {} Recipes of Type {}", total - unhandled.size(), total, typeRegistryName);
			if (!unhandled.isEmpty()) {
				PECore.debugLog("Unhandled Recipes of Type {}:", typeRegistryName);
				for (RecipeHolder<?> recipeHolder : unhandled) {
					PECore.debugLog("Name: {}, Recipe class: {}", recipeHolder.id(), recipeHolder.value().getClass().getName());
				}
			}
		}
		for (ResourceLocation typeRegistryName : canNotMap) {
			PECore.debugLog("Could not map any Recipes of Type: {}", typeRegistryName);
		}
		NSSFake.resetNamespace();
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER.tooltip();
	}

	private static class RecipeCountInfo {

		private final int totalRecipes;
		private List<RecipeHolder<?>> unhandled;

		private RecipeCountInfo(int totalRecipes, List<RecipeHolder<?>> unhandled) {
			this.totalRecipes = totalRecipes;
			this.unhandled = unhandled;
		}

		public int getTotalRecipes() {
			return totalRecipes;
		}

		public void setUnhandled(List<RecipeHolder<?>> unhandled) {
			this.unhandled = unhandled;
		}

		public List<RecipeHolder<?>> getUnhandled() {
			return unhandled;
		}
	}

	private static class NSSFakeGroupManager implements INSSFakeGroupManager {

		private final Map<Set<NormalizedSimpleStack>, FakeGroupData> groups = new HashMap<>();
		private final Map<Object2IntMap<NormalizedSimpleStack>, FakeGroupData> groupsWithCount = new HashMap<>();
		private int fakeIndex;

		@Override
		public FakeGroupData getOrCreateFakeGroup(Set<NormalizedSimpleStack> normalizedSimpleStacks) {
			return getOrCreateFakeGroup(groups, normalizedSimpleStacks, HashSet::new);
		}

		@Override
		public FakeGroupData getOrCreateFakeGroup(Object2IntMap<NormalizedSimpleStack> normalizedSimpleStacks) {
			return getOrCreateFakeGroup(groupsWithCount, normalizedSimpleStacks, Object2IntOpenHashMap::new);
		}

		private <COLLECTION> FakeGroupData getOrCreateFakeGroup(Map<COLLECTION, FakeGroupData> groups, COLLECTION stacks, UnaryOperator<COLLECTION> copyFunction) {
			FakeGroupData data = groups.get(stacks);
			if (data == null) {
				//Doesn't exist, create one with the next index add it as known and return
				// the group and the fact that we had to create a representation for it
				// Note: We use an incrementing index here as our crafting mapper sets a namespace
				// for NSSFake objects, so we can safely use integers as the description and not
				// have to worry about intersecting fake stacks. We also for good measure specify in
				// the IRecipeTypeMapper java docs that if fake stacks are needed by an implementer
				// they should make sure to make the name more complex than just a simple integer to
				// ensure that they do not collide with stacks created by this method.
				NormalizedSimpleStack stack = NSSFake.create(Integer.toString(fakeIndex++));
				//Copy the set into a new set to ensure that it can't be modified by changing
				// the set that was passed in
				//TODO - 1.21: What map implementation do we want to use? And do we even want to have to be copying it?
				//Note: We put that it wasn't created in the map, so when it is retrieved, we know this wasn't the first time
				groups.put(copyFunction.apply(stacks), new FakeGroupData(stack, false));
				return new FakeGroupData(stack, true);
			}
			return data;
		}
	}
}