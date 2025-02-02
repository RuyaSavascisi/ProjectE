package moze_intel.projecte.emc.mappers.recipe.special;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager.FakeGroupData;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.crafting.SuspiciousStewRecipe;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jetbrains.annotations.Nullable;

@RecipeTypeMapper
public class SuspiciousStewMapper extends SpecialRecipeMapper<SuspiciousStewRecipe> {

	private static final ResourceLocation SUSPICIOUS_STEW = BuiltInRegistries.ITEM.getKey(Items.SUSPICIOUS_STEW);

	@Override
	protected Class<SuspiciousStewRecipe> getRecipeClass() {
		return SuspiciousStewRecipe.class;
	}

	@Override
	protected boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
		Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(ItemTags.SMALL_FLOWERS);
		if (tag.isPresent()) {
			Named<Item> flowersTag = tag.get();
			NSSItem nssBowl = NSSItem.createItem(Items.BOWL);
			NSSItem nssRedMushroom = NSSItem.createItem(Items.RED_MUSHROOM);
			NSSItem nssBrownMushroom = NSSItem.createItem(Items.BROWN_MUSHROOM);
			Map<@Nullable SuspiciousStewEffects, Set<NormalizedSimpleStack>> knownEffects = new HashMap<>(flowersTag.size());
			for (Holder<Item> flower : flowersTag) {
				SuspiciousEffectHolder effectHolder = SuspiciousEffectHolder.tryGet(flower.value());
				knownEffects.computeIfAbsent(effectHolder == null ? null : effectHolder.getSuspiciousEffects(), k -> new HashSet<>())
						.add(NSSItem.createItem(flower));
			}
			for (Map.Entry<@Nullable SuspiciousStewEffects, Set<NormalizedSimpleStack>> entry : knownEffects.entrySet()) {
				Set<NormalizedSimpleStack> flowers = entry.getValue();
				NSSItem nssStew = createStew(entry.getKey());
				NormalizedSimpleStack nssFlower;
				if (flowers.size() == 1) {
					nssFlower = flowers.iterator().next();
				} else {
					//Create a recipe using dummy ingredients for any stew recipes that share an effect
					// Note: We use a fake group in case any recipes just happen to use only these flowers in them
					FakeGroupData group = fakeGroupManager.getOrCreateFakeGroupDirect(flowers);
					nssFlower = group.dummy();
					if (group.created()) {
						for (NormalizedSimpleStack flower : flowers) {
							mapper.addConversion(1, nssFlower, EMCHelper.intMapOf(flower, 1));
						}
					}
				}
				mapper.addConversion(1, nssStew, EMCHelper.intMapOf(
						nssBowl, 1,
						nssRedMushroom, 1,
						nssBrownMushroom, 1,
						nssFlower, 1
				));
			}
			PECore.debugLog("{} Statistics:", getName());
			PECore.debugLog("Found {} Suspicious Stew Recipes", knownEffects.size());
		}
		return true;
	}

	private NSSItem createStew(@Nullable SuspiciousStewEffects effects) {
		if (effects == null) {
			return NSSItem.createItem(SUSPICIOUS_STEW);
		}
		return NSSItem.createItem(SUSPICIOUS_STEW, DataComponentPatch.builder()
				.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, effects)
				.build()
		);
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_SUSPICIOUS_STEW.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_SUSPICIOUS_STEW.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_CRAFTING_MAPPER_SUSPICIOUS_STEW.tooltip();
	}
}