package moze_intel.projecte.emc.components.processor;

import java.util.Map;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

@DataComponentProcessor
public class EnchantmentProcessor extends PersistentComponentProcessor<ItemEnchantments> {

	private static final ResourceKey<Item> ENCHANTED_BOOK = BuiltInRegistries.ITEM.getResourceKey(Items.ENCHANTED_BOOK).orElseThrow();
	private static final long ENCH_EMC_BONUS = 5_000;

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_ENCHANTMENT.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_ENCHANTMENT.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_ENCHANTMENT.tooltip();
	}

	@Override
	public boolean isAvailable() {
		//Disable by default
		return false;
	}

	@Override
	public boolean usePersistentComponents() {
		//Disable by default
		return false;
	}

	@Override
	public long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull ItemEnchantments enchantments) throws ArithmeticException {
		for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
			int rarityWeight = entry.getKey().value().definition().weight();
			if (rarityWeight > 0) {
				currentEMC = Math.addExact(currentEMC, Math.multiplyExact(ENCH_EMC_BONUS / rarityWeight, entry.getValue()));
			}
		}
		return currentEMC;
	}

	@Override
	protected DataComponentType<ItemEnchantments> getComponentType(@NotNull ItemInfo info) {
		return info.getItem().is(ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
	}

	@Override
	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull ItemEnchantments component) {
		return !component.isEmpty();
	}
}