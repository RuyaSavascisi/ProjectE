package moze_intel.projecte.api.world_transmutation;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface IWorldTransmutation permits SimpleWorldTransmutation, WorldTransmutation {

	/**
	 * {@return whether this world transmutation has an alternate output when sneaking}
	 */
	boolean hasAlternate();

	/**
	 * Gets the result of applying this world transmutation to a given state.
	 *
	 * @param state      Input state to try and match.
	 * @param isSneaking Whether the player is sneaking and the alternate result should be used if present.
	 *
	 * @return The resulting state, or {@code null} if {@link #canTransmute(BlockState)} returns {@code false} for the input state.
	 */
	@Nullable
	BlockState result(@NotNull BlockState state, boolean isSneaking);

	/**
	 * Checks whether this world transmutation can be applied to a given block state.
	 *
	 * @param state Input state to try and match.
	 *
	 * @return {@code true} if this world transmutation is valid for the given state.
	 */
	boolean canTransmute(@NotNull BlockState state);
}