package moze_intel.projecte.integration.jei.world_transmute;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.world_transmutation.IWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.SimpleWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.WorldTransmutation;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class WorldTransmuteEntry {

	private record StateInfo(ItemStack item, FluidStack fluid) {

		public boolean isEmpty() {
			return item.isEmpty() && fluid.isEmpty();
		}

		public Either<ItemStack, FluidStack> toEither() {
			if (fluid.isEmpty()) {
				return Either.left(item);
			}
			return Either.right(fluid);
		}
	}

	private static final StateInfo EMPTY = new StateInfo(ItemStack.EMPTY, FluidStack.EMPTY);

	private final StateInfo input;
	private final StateInfo leftOutput;
	private final StateInfo rightOutput;

	public WorldTransmuteEntry(IWorldTransmutation transmutation) {
		if (transmutation instanceof SimpleWorldTransmutation(Holder<Block> origin, Holder<Block> result, Holder<Block> altResult)) {
			input = createInfo(origin.value());
			leftOutput = createInfo(result.value());
			if (transmutation.hasAlternate()) {
				rightOutput = createInfo(altResult.value());
			} else {
				rightOutput = EMPTY;
			}
		} else if (transmutation instanceof WorldTransmutation(BlockState originState, BlockState result, BlockState altResult)) {
			input = createInfo(originState);
			leftOutput = createInfo(result);
			if (transmutation.hasAlternate()) {
				rightOutput = createInfo(altResult);
			} else {
				rightOutput = EMPTY;
			}
		} else {
			throw new IllegalStateException("Unknown transmutation implementation: " + transmutation);
		}
	}

	private StateInfo createInfo(Block output) {
		FluidStack outputFluid = fluidFromBlock(output);
		if (outputFluid.isEmpty()) {
			return new StateInfo(new ItemStack(output), outputFluid);
		}
		return new StateInfo(ItemStack.EMPTY, outputFluid);
	}

	private StateInfo createInfo(BlockState output) {
		FluidStack outputFluid = fluidFromBlock(output.getBlock());
		if (outputFluid.isEmpty()) {
			return new StateInfo(itemFromBlock(output.getBlock(), output), outputFluid);
		}
		return new StateInfo(ItemStack.EMPTY, outputFluid);
	}

	private FluidStack fluidFromBlock(Block block) {
		if (block instanceof LiquidBlock liquidBlock) {
			return new FluidStack(liquidBlock.fluid, FluidType.BUCKET_VOLUME);
		}
		return FluidStack.EMPTY;
	}

	private ItemStack itemFromBlock(Block block, BlockState state) {
		try {
			//We don't have a world or position, but try pick block anyways
			return block.getCloneItemStack(state, null, null, null, null);
		} catch (Exception e) {
			//It failed, probably because of the null world and pos
			return new ItemStack(block);
		}
	}

	public boolean isRenderable() {
		return hasInput() && (!leftOutput.isEmpty() || !rightOutput.isEmpty());
	}

	public boolean hasInput() {
		return !input.isEmpty();
	}

	public Either<ItemStack, FluidStack> getInput() {
		if (input.isEmpty()) {
			throw new IllegalStateException("getInput called with empty input");
		}
		return input.toEither();
	}

	public Iterable<Either<ItemStack, FluidStack>> getOutput() {
		List<Either<ItemStack, FluidStack>> outputs = new ArrayList<>(2);
		if (!leftOutput.isEmpty()) {
			outputs.add(leftOutput.toEither());
		}
		if (!rightOutput.isEmpty()) {
			outputs.add(rightOutput.toEither());
		}
		return outputs;
	}
}