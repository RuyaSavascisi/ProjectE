package moze_intel.projecte.api.world_transmutation;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface IWorldTransmutation permits SimpleWorldTransmutation, WorldTransmutation {

	//TODO - 1.21: Docs
	boolean hasAlternate();

	@Nullable
	BlockState result(@NotNull BlockState state, boolean isSneaking);

	boolean canTransmute(@NotNull BlockState state);
}