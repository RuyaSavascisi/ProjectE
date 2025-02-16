package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.config.PEConfigTranslations;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@DataComponentProcessor
public class StoredEMCProcessor implements IDataComponentProcessor {

	@Override
	public String getName() {
		return PEConfigTranslations.DCP_STORED_EMC.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.DCP_STORED_EMC.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.DCP_STORED_EMC.tooltip();
	}

	@Override
	@Range(from = 0, to = Long.MAX_VALUE)
	public long recalculateEMC(@NotNull ItemInfo info, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC) throws ArithmeticException {
		ItemStack stack = info.createStack();
		IItemEmcHolder emcHolder = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
		if (emcHolder != null) {
			return Math.addExact(currentEMC, emcHolder.getStoredEmc(stack));
		}
		return currentEMC;
	}
}