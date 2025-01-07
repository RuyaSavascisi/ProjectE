package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@DataComponentProcessor(priority = Integer.MAX_VALUE)
public class DamageProcessor implements IDataComponentProcessor {

	@Override
	public String getName() {
		return "DamageProcessor";
	}

	@Override
	public String getDescription() {
		return "Reduces the EMC value the more damaged an item is.";
	}

	@Override
	public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
		ItemStack fakeStack = info.createStack();
		int maxDamage = fakeStack.getMaxDamage();
		if (maxDamage > 0) {
			int damage = fakeStack.getDamageValue();
			if (damage > maxDamage) {
				//This may happen if mods implement their custom damage values incorrectly
				throw new ArithmeticException();
			}
			//maxDmg + 1 because vanilla lets you use the tool one more time
			// when item damage == max damage (shows as Durability: 0 / max)
			currentEMC = Math.multiplyExact(currentEMC, Math.addExact(maxDamage - damage, 1)) / maxDamage;
		}
		return currentEMC;
	}
}