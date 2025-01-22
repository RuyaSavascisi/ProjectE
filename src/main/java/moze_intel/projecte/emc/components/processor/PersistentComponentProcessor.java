package moze_intel.projecte.emc.components.processor;

import java.util.Optional;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.NotNull;

public abstract class PersistentComponentProcessor<TYPE> implements IDataComponentProcessor {

	protected abstract long recalculateEMC(@NotNull ItemInfo info, long currentEMC, @NotNull TYPE component) throws ArithmeticException;

	protected abstract DataComponentType<TYPE> getComponentType(@NotNull ItemInfo info);

	protected boolean validItem(@NotNull ItemInfo info) {
		return true;
	}

	protected boolean shouldPersist(@NotNull ItemInfo info, @NotNull TYPE component) {
		return true;
	}

	@Override
	public boolean hasPersistentComponents() {
		return true;
	}

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

	@Override
	public final void collectPersistentComponents(@NotNull ItemInfo info, @NotNull DataComponentPatch.Builder builder) {
		if (validItem(info)) {
			DataComponentType<TYPE> componentType = getComponentType(info);
			//TODO - 1.21: Re-evaluate how we interact with the component patch here and in recalculateEMC,
			// should we allow persisting and using default components?
			// The damage processor semi gets around this by creating the fake stack to see if it is damageable
			Optional<? extends TYPE> storedComponent = info.getComponentsPatch().get(componentType);
			if (storedComponent != null && storedComponent.isPresent()) {
				TYPE component = storedComponent.get();
				if (shouldPersist(info, component)) {
					builder.set(componentType, component);
				}
			}
		}
	}
}