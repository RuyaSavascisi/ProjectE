package moze_intel.projecte.emc.components.processor;

import java.util.Optional;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.NotNull;

public abstract class PersistentComponentProcessor<TYPE> extends SimplePersistentComponentProcessor<TYPE> {

	protected abstract long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull TYPE component) throws ArithmeticException;

	@Override
	public final long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
		if (validItem(info)) {
			DataComponentType<TYPE> componentType = getComponentType(info);
			Optional<? extends TYPE> storedComponent = info.getComponentsPatch().get(componentType);
			if (storedComponent != null && storedComponent.isPresent()) {
				TYPE component = storedComponent.get();
				if (shouldPersist(info, component)) {
					return recalculateEMC(info, currentEMC, component);
				}
			}
		}
		return currentEMC;
	}
}