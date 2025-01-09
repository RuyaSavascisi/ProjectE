package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.jetbrains.annotations.NotNull;

@DataComponentProcessor
public class ArmorTrimProcessor extends PersistentComponentProcessor<ArmorTrim> {

	@Override
	public String getName() {
		return "ArmorTrimProcessor";
	}

	@Override
	public String getDescription() {
		return "Calculates EMC value of trimmed armor.";
	}

	@Override
	public long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull ArmorTrim trim) throws ArithmeticException {
		Holder<Item> material = trim.material().value().ingredient();
		Holder<Item> template = trim.pattern().value().templateItem();
		return Math.addExact(
				Math.addExact(currentEMC, EMCHelper.getEmcValue(material)),
				EMCHelper.getEmcValue(template)
		);
	}

	@Override
	protected boolean validItem(@NotNull ItemInfo info) {
		return info.getItem().is(ItemTags.TRIMMABLE_ARMOR);
	}

	@Override
	protected DataComponentType<ArmorTrim> getComponentType(@NotNull ItemInfo info) {
		return DataComponents.TRIM;
	}
}
