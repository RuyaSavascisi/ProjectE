package moze_intel.projecte.emc.components.processor;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.components.DataComponentManager;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.NotNull;

public abstract class PersistentComponentProcessor<TYPE> extends SimplePersistentComponentProcessor<TYPE> {

	protected abstract long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull TYPE component) throws ArithmeticException;

	@Override
	public final long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
		if (validItem(info)) {
			DataComponentType<TYPE> componentType = getComponentType(info);
			//TODO - 1.21: Re-evaluate how we interact with the component patch here. Should we allow using values from the default components?
			// The damage processor semi gets around this by creating the fake stack to see if it is damageable
			// Another thing is this method doesn't even get called if there are no modified components, so maybe we shouldn't?
			TYPE component = DataComponentManager.getOrNull(info.getComponentsPatch(), componentType);
			if (component != null && shouldPersist(info, component)) {
				return recalculateEMC(info, currentEMC, component);
			}
		}
		return currentEMC;
	}
}