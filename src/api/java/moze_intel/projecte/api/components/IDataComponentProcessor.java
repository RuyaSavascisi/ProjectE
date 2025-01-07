package moze_intel.projecte.api.components;

import moze_intel.projecte.api.ItemInfo;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.NotNull;

/**
 * Class used for processing what Data Components modifies the EMC value, and what Data Components are needed/should be saved when transmuting an item.
 */
public interface IDataComponentProcessor {

	/**
	 * A unique Name for the {@link IDataComponentProcessor}. This is used to identify the {@link IDataComponentProcessor} in the Configuration.
	 *
	 * @return A unique Name
	 */
	String getName();

	/**
	 * A Description, that will be included as a Comment in the Configuration File
	 *
	 * @return A <b>short</b> description
	 */
	String getDescription();

	/**
	 * This method is used to determine the default for enabling/disabling this {@link IDataComponentProcessor}. If this returns {@code false} neither
	 * {@link #collectPersistentComponents(ItemInfo, DataComponentPatch.Builder)} nor {@link #recalculateEMC(ItemInfo, long)} will not be called.
	 *
	 * @return {@code true} if you want this {@link IDataComponentProcessor} to be part of the EMC calculations, {@code false} otherwise.
	 */
	default boolean isAvailable() {
		return true;
	}

	/**
	 * This method is used to determine if this {@link IDataComponentProcessor} can ever have persistent data. If this returns {@code false}
	 * {@link #usePersistentComponents()} will not be checked.<br/>
	 *
	 * @return {@code true} if you want {@link #usePersistentComponents()} to be checked, {@code false} otherwise.
	 */
	default boolean hasPersistentComponents() {
		return false;
	}

	/**
	 * This method is used to determine if this {@link IDataComponentProcessor} should contribute its persistent data. If this returns {@code false}
	 * {@link #collectPersistentComponents(ItemInfo, DataComponentPatch.Builder)} will not be called.<br/>
	 *
	 * This method will also be used to determine the default for enabling/disabling of Data Component persistence this {@link IDataComponentProcessor}
	 *
	 * @return {@code true} if you want {@link #collectPersistentComponents(ItemInfo, DataComponentPatch.Builder)} to be called, {@code false} otherwise.
	 */
	default boolean usePersistentComponents() {
		return hasPersistentComponents();
	}

	/**
	 * Calculates any changes to EMC this {@link IDataComponentProcessor} has to make based on the given {@link ItemInfo}
	 *
	 * @param info       The {@link ItemInfo} to attempt to get any Data Components specific information this
	 * @param currentEMC The EMC value before this {@link IDataComponentProcessor} has performed any calculations.
	 *
	 * @return The EMC value after this {@link IDataComponentProcessor} has performed its calculations.
	 *
	 * @throws ArithmeticException If an overflow happened or some calculation went really bad and we should just hard exit and return the last successful EMC value
	 *                             calculated.
	 */
	long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException;

	/**
	 * Collects the minimum set of data components that are needed to recreate/get an EMC value from this {@link IDataComponentProcessor} for an {@link ItemInfo}. This is
	 * used for building up the actual {@link ItemInfo} that will be saved to Knowledge/duplication in a condenser.
	 *
	 * @param info The {@link ItemInfo} to get the persistent components from.
	 */
	default void collectPersistentComponents(@NotNull ItemInfo info, @NotNull DataComponentPatch.Builder builder) {
	}
}