package moze_intel.projecte.emc.mappers;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

@EMCMapper
public class BrewingMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		//TODO - 1.21: Figure this out? At the very least add a warning that gets logged
		PotionBrewing potionBrewing = server == null ? PotionBrewing.EMPTY : server.potionBrewing();
		Set<ItemInfo> allReagents = mapAllReagents(potionBrewing);
		Set<ItemInfo> allInputs = mapAllInputs(potionBrewing);

		//Add conversion for empty bottle + water to water bottle
		Object2IntMap<NormalizedSimpleStack> waterIngredients = new Object2IntOpenHashMap<>();
		waterIngredients.put(NSSItem.createItem(Items.GLASS_BOTTLE), 1);
		waterIngredients.put(NSSFluid.createTag(FluidTags.WATER), FluidType.BUCKET_VOLUME / 3);
		mapper.addConversion(1, NSSItem.createItem(PotionContents.createItemStack(Items.POTION, Potions.WATER)), waterIngredients);

		int recipeCount = 0;


		//Check all known valid inputs and reagents to see which ones create valid inputs
		// Note: Getting the list of outputs while getting reagents will cause things to be missed
		// As the PotionBrewing class does not contain all valid mappings (For example: Potion of luck + gunpowder -> splash potion of luck)
		for (ItemInfo inputInfo : allInputs) {
			ItemStack validInput = inputInfo.createStack();
			NormalizedSimpleStack nssInput = NSSItem.createItem(validInput);
			for (ItemInfo reagentInfo : allReagents) {
				ItemStack validReagent = reagentInfo.createStack();
				ItemStack output = potionBrewing.mix(validInput, validReagent);
				if (!output.isEmpty()) {
					Object2IntMap<NormalizedSimpleStack> ingredientsWithAmount = new Object2IntOpenHashMap<>();
					ingredientsWithAmount.put(nssInput, 3);
					ingredientsWithAmount.put(NSSItem.createItem(validReagent), 1);
					//Add the conversion, 3 input + reagent = 3 y output as the output technically could be stacked
					mapper.addConversion(3 * output.getCount(), NSSItem.createItem(output), ingredientsWithAmount);
					recipeCount++;
				}
			}
		}

		Set<Class<?>> canNotMap = new HashSet<>();
		for (IBrewingRecipe recipe : potionBrewing.getRecipes()) {
			if (recipe instanceof BrewingRecipe brewingRecipe) {
				ItemStack[] validInputs = getMatchingStacks(brewingRecipe.getInput());
				ItemStack[] validReagents = getMatchingStacks(brewingRecipe.getIngredient());
				if (validInputs == null || validReagents == null) {
					//Skip brewing recipes that we are not able to process such as ones using tags
					// as ingredients, as tags don't exist when the brewing recipe is being defined
					continue;
				}
				ItemStack output = brewingRecipe.getOutput();
				NormalizedSimpleStack nssOut = NSSItem.createItem(output);
				for (ItemStack validInput : validInputs) {
					NormalizedSimpleStack nssInput = NSSItem.createItem(validInput);
					for (ItemStack validReagent : validReagents) {
						Object2IntMap<NormalizedSimpleStack> ingredientsWithAmount = new Object2IntOpenHashMap<>();
						ingredientsWithAmount.put(nssInput, 3);
						ingredientsWithAmount.put(NSSItem.createItem(validReagent), validReagent.getCount());
						//Add the conversion, 3 input + x reagent = 3 y output as strictly speaking the only one of the three parts
						// in the recipe that are required to be one in stack size is the input
						mapper.addConversion(3 * output.getCount(), nssOut, ingredientsWithAmount);
						recipeCount++;
					}
				}
			} else {
				canNotMap.add(recipe.getClass());
			}
		}

		PECore.debugLog("BrewingMapper Statistics:");
		PECore.debugLog("Found {} Brewing Recipes", recipeCount);
		for (Class<?> c : canNotMap) {
			PECore.debugLog("Could not map Brewing Recipes with Type: {}", c.getName());
		}
	}

	@Override
	public String getName() {
		return "BrewingMapper";
	}

	@Override
	public String getDescription() {
		return "Add Conversions for Brewing Recipes";
	}

	@Nullable
	private static ItemStack[] getMatchingStacks(Ingredient ingredient) {
		try {
			return ingredient.getItems();
		} catch (Exception e) {
			return null;
		}
	}

	private Set<ItemInfo> mapAllReagents(PotionBrewing potionBrewing) {
		Set<ItemInfo> allReagents = new HashSet<>();
		addReagents(allReagents, potionBrewing.containerMixes);
		addReagents(allReagents, potionBrewing.potionMixes);
		return allReagents;
	}

	private <T> void addReagents(Set<ItemInfo> allReagents, List<PotionBrewing.Mix<T>> conversions) {
		for (PotionBrewing.Mix<T> conversion : conversions) {
			for (ItemStack r : conversion.ingredient().getItems()) {
				allReagents.add(ItemInfo.fromStack(r));
			}
		}
	}

	private Set<ItemInfo> mapAllInputs(PotionBrewing potionBrewing) {
		Set<ItemInfo> allInputs = new HashSet<>();

		Set<ItemInfo> inputs = new HashSet<>();
		for (Ingredient potionItem : potionBrewing.containers) {
			ItemStack[] matchingStacks = getMatchingStacks(potionItem);
			if (matchingStacks != null) {
				//Silently ignore any invalid potion items (ingredients that may be tags) this should never be the case
				// unless someone ATs the map and inserts a custom ingredient into it, but just in case, don't crash
				for (ItemStack input : matchingStacks) {
					inputs.add(ItemInfo.fromStack(input));
				}
			}
		}
		for (Holder<Potion> potion : BuiltInRegistries.POTION.holders().toList()) {
			PotionContents contents = new PotionContents(potion);
			for (ItemInfo input : inputs) {
				ItemStack stack = input.createStack();
				stack.set(DataComponents.POTION_CONTENTS, contents);
				allInputs.add(ItemInfo.fromStack(stack));
			}
		}
		return allInputs;
	}
}