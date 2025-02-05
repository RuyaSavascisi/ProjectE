package moze_intel.projecte.api.capabilities.item;

import moze_intel.projecte.api.PEDataComponents;
import moze_intel.projecte.api.PESounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface specifies items that have a charge that changes when the respective keybinding is activated (default V)
 * <p>
 * This is exposed through the Capability system.
 * <p>
 * Acquire an instance of this using {@link ItemStack#getCapability(ItemCapability)}.
 */
public interface IItemCharge {

	int getNumCharges(@NotNull ItemStack stack);

	/**
	 * Gets the current percent charge on the given ItemStack. Should be a value between 0 and 1 inclusive.
	 *
	 * @param stack Stack whose charge percent we want
	 *
	 * @return The percent charge on the stack
	 */
	default float getChargePercent(@NotNull ItemStack stack) {
		return (float) getCharge(stack) / getNumCharges(stack);
	}

	/**
	 * Returns the current charge on the given ItemStack
	 *
	 * @param stack Stack whose charge we want
	 *
	 * @return The charge on the stack
	 */
	default int getCharge(@NotNull ItemStack stack) {
		return Mth.clamp(stack.getOrDefault(PEDataComponents.CHARGE, 0), 0, getNumCharges(stack));
	}

	/**
	 * Called serverside when the player presses the charge keybinding; reading sneaking state is up to you
	 *
	 * @param player The player
	 * @param stack  The item being charged
	 * @param hand   The hand this stack was in, or null if the call was not from the player's hands
	 *
	 * @return Whether the operation succeeded
	 */
	default boolean changeCharge(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand) {
		int currentCharge = getCharge(stack);
		int numCharges = getNumCharges(stack);

		if (player.isSecondaryUseActive()) {
			if (currentCharge > 0) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PESounds.UNCHARGE.value(), SoundSource.PLAYERS, 1.0F,
						0.5F + ((0.5F / (float) numCharges) * currentCharge));
				stack.set(PEDataComponents.CHARGE, currentCharge - 1);
				return true;
			}
		} else if (currentCharge < numCharges) {
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PESounds.CHARGE.value(), SoundSource.PLAYERS, 1.0F,
					0.5F + ((0.5F / (float) numCharges) * currentCharge));
			stack.set(PEDataComponents.CHARGE, currentCharge + 1);
			return true;
		}
		return false;
	}
}