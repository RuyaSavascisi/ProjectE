package moze_intel.projecte.integration.jei.collectors;

import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.world.item.ItemStack;

public record FuelUpgradeRecipe(ItemStack input, ItemStack output, long upgradeEMC) {

	public FuelUpgradeRecipe(ItemStack input, ItemStack output) {
		this(input, output, IEMCProxy.INSTANCE.getValue(output) - IEMCProxy.INSTANCE.getValue(input));
	}
}