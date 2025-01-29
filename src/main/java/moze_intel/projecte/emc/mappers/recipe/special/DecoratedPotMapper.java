package moze_intel.projecte.emc.mappers.recipe.special;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Optional;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager.FakeGroupData;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.DecoratedPotRecipe;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;

@RecipeTypeMapper//TODO - 1.21: Figure out if we want to do it this way or via the component processor
public class DecoratedPotMapper extends SpecialRecipeMapper<DecoratedPotRecipe> {

	@Override
	protected Class<DecoratedPotRecipe> getRecipeClass() {
		return DecoratedPotRecipe.class;
	}

	@Override
	protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
		Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(ItemTags.DECORATED_POT_INGREDIENTS);
		if (tag.isEmpty()) {
			return false;
		}
		//TODO - 1.21: With this mapper
		// Registered 333225 EMC values. (took 6678 ms)
		// With this mapper, making use of dummy groups
		// Registered 333225 EMC values. (took 3731 ms)
		// Without this mapper:
		// Registered 1450 EMC values. (took 239 ms)
		int recipeCount = 0;
		int uniqueInputs = 0;
		Named<Item> ingredients = tag.get();
		for (Holder<Item> back : ingredients) {
			NSSItem nssBack = NSSItem.createItem(back);
			for (Holder<Item> left : ingredients) {
				NSSItem nssLeft = NSSItem.createItem(left);
				for (Holder<Item> right : ingredients) {
					NSSItem nssRight = NSSItem.createItem(right);
					for (Holder<Item> front : ingredients) {
						NSSItem nssFront = NSSItem.createItem(front);
						PotDecorations decorations = new PotDecorations(back.value(), left.value(), right.value(), front.value());
						NSSItem nssDecorated = NSSItem.createItem(DecoratedPotBlockEntity.createDecoratedPotItem(decorations));
						//TODO - 1.21: Can we batch things using the fake group manager like BaseRecipeTypeMapper does
						// Might be able to at least do them as a fake group for unique variants
						Object2IntMap<NormalizedSimpleStack> nssIngredients = getIngredients(nssBack, nssLeft, nssRight, nssFront);
						FakeGroupData group = fakeGroupManager.getOrCreateFakeGroup(nssIngredients);
						mapper.addConversion(1, nssDecorated, Object2IntMaps.singleton(group.dummy(), 1));
						recipeCount++;
						if (group.created()) {
							mapper.addConversion(1, group.dummy(), nssIngredients);
							uniqueInputs++;
						}
					}
				}
			}
		}
		PECore.debugLog("{} Statistics:", getName());
		PECore.debugLog("Found {} Decorated Pot Combinations. With {} unique combinations.", recipeCount, uniqueInputs);
		return true;
	}

	private Object2IntMap<NormalizedSimpleStack> getIngredients(NSSItem nssBack, NSSItem nssLeft, NSSItem nssRight, NSSItem nssFront) {
		Object2IntMap<NormalizedSimpleStack> ingredients = new Object2IntArrayMap<>(4);
		ingredients.put(nssBack, 1);
		ingredients.mergeInt(nssLeft, 1, Constants.INT_SUM);
		ingredients.mergeInt(nssRight, 1, Constants.INT_SUM);
		ingredients.mergeInt(nssFront, 1, Constants.INT_SUM);
		return ingredients;
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_DECORATED_POT.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_DECORATED_POT.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_DECORATED_POT.tooltip();
	}
}