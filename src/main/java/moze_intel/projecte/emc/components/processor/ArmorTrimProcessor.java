package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.config.PEConfigTranslations;
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
		return PEConfigTranslations.DCP_ARMOR_TRIM.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_ARMOR_TRIM.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_ARMOR_TRIM.tooltip();
	}

	@Override
	public long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull ArmorTrim trim) throws ArithmeticException {
		Holder<Item> material = trim.material().value().ingredient();
		Holder<Item> template = trim.pattern().value().templateItem();
		//TODO - 1.21: Re-evaluate all these processors and any that get the emc value of another thing, we likely want to have the result fail if one of the parts didn't have one
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
