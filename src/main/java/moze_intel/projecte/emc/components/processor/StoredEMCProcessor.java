package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.components.DataComponentProcessor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@DataComponentProcessor
public class StoredEMCProcessor implements IDataComponentProcessor {

	@Override
	public String getName() {
		return "StoredEMCProcessor";
	}

	@Override
	public String getDescription() {
		return "Increases the EMC value of the item to take into account any EMC the item has stored.";
	}

	@Override
	public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
		ItemStack stack = info.createStack();
		IItemEmcHolder emcHolder = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
		if (emcHolder != null) {
			return Math.addExact(currentEMC, emcHolder.getStoredEmc(stack));
		}
		return currentEMC;
	}
}