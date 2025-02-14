package moze_intel.projecte.api.data;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import moze_intel.projecte.api.world_transmutation.IWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.SimpleWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.WorldTransmutation;
import moze_intel.projecte.api.world_transmutation.WorldTransmutationFile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorldTransmutationBuilder extends BaseFileBuilder<WorldTransmutationBuilder> {

	private final Set<IWorldTransmutation> transmutationEntries = new LinkedHashSet<>();

	WorldTransmutationBuilder() {
		super("World Transmutation");
	}

	WorldTransmutationFile build() {
		return new WorldTransmutationFile(comment, List.copyOf(transmutationEntries));
	}

	private WorldTransmutationBuilder register(@NotNull IWorldTransmutation transmutation) {
		//TODO - 1.21: Prevent against duplicate input blocks as well?
		if (!transmutationEntries.add(transmutation)) {
			throw new IllegalStateException("World transmutation file contains duplicate transmutations.");
		}
		return this;
	}

	/**
	 * Registers a world transmutation from an exact match of the given block state to the resulting block state.
	 *
	 * @param from   Origin block state.
	 * @param result Resulting block state.
	 */
	public WorldTransmutationBuilder register(BlockState from, BlockState result) {
		if (from.getValues().isEmpty() && result.getValues().isEmpty()) {
			throw new IllegalArgumentException("None of the provided states have any properties, use the block based register method.");
		} else if (from == result) {
			throw new IllegalArgumentException("Cannot register a world transmutation from a block to itself.");
		}
		return register(new WorldTransmutation(from, result));
	}

	/**
	 * Registers a world transmutation from an exact match of the given block state to the resulting block state and secondary resulting state.
	 *
	 * @param from      Origin block state.
	 * @param result    Resulting block state.
	 * @param altResult Alternate resulting state.
	 */
	public WorldTransmutationBuilder register(BlockState from, BlockState result, BlockState altResult) {
		if (from.getValues().isEmpty() && result.getValues().isEmpty() && altResult.getValues().isEmpty()) {
			throw new IllegalArgumentException("None of the provided states have any properties, use the block based register method.");
		} else if (from == result || from == altResult) {
			throw new IllegalArgumentException("Cannot register a world transmutation from a block to itself.");
		}
		return register(new WorldTransmutation(from, result, altResult));
	}

	/**
	 * Registers a world transmutation for the given block (with any state) to the resulting block. Any properties that exist on both blocks will be transferred when
	 * transmuting.
	 *
	 * @param from   Origin block.
	 * @param result Resulting block.
	 */
	@SuppressWarnings("deprecation")
	public WorldTransmutationBuilder register(Block from, Block result) {
		return register(from.builtInRegistryHolder(), result.builtInRegistryHolder());
	}

	/**
	 * Registers a world transmutation for the given block (with any state) to the resulting block and secondary result. Any properties that exist on both blocks will be
	 * transferred when transmuting.
	 *
	 * @param from      Origin block.
	 * @param result    Resulting block.
	 * @param altResult Alternate resulting.
	 */
	@SuppressWarnings("deprecation")
	public WorldTransmutationBuilder register(Block from, Block result, Block altResult) {
		return register(from.builtInRegistryHolder(), result.builtInRegistryHolder(), altResult.builtInRegistryHolder());
	}

	/**
	 * Registers a world transmutation for the given block (with any state) to the resulting block. Any properties that exist on both blocks will be transferred when
	 * transmuting.
	 *
	 * @param from   Origin block.
	 * @param result Resulting block.
	 */
	public WorldTransmutationBuilder register(Holder<Block> from, Holder<Block> result) {
		 if (from.is(result)) {
			throw new IllegalArgumentException("Cannot register a world transmutation from a block to itself.");
		}
		return register(new SimpleWorldTransmutation(from, result));
	}

	/**
	 * Registers a world transmutation for the given block (with any state) to the resulting block and secondary result. Any properties that exist on both blocks will be
	 * transferred when transmuting.
	 *
	 * @param from      Origin block.
	 * @param result    Resulting block.
	 * @param altResult Alternate resulting.
	 */
	public WorldTransmutationBuilder register(Holder<Block> from, Holder<Block> result, Holder<Block> altResult) {
		if (from.is(result) || from.is(altResult)) {
			throw new IllegalArgumentException("Cannot register a world transmutation from a block to itself.");
		}
		return register(new SimpleWorldTransmutation(from, result, altResult));
	}

	/**
	 * Registers world transmutations for all sequential blocks (including the last element to the first). Each registered transmutation will match any state and any
	 * properties that exist on both blocks will be transferred when transmuting.
	 *
	 * @param blocks List of blocks to register world transmutations for.
	 *
	 * @apiNote If this is called with only two elements, this will effectively just register two transmutations to convert one block into the other and one to convert
	 * the other back into the first one.
	 */
	@SafeVarargs
	public final WorldTransmutationBuilder registerConsecutivePairs(Holder<Block>... blocks) {
		if (blocks.length < 2) {
			throw new IllegalArgumentException("Expected at least two blocks for registering consecutive pairs");
		}
		for (int i = 0; i < blocks.length; i++) {
			Holder<Block> prev = i == 0 ? blocks[blocks.length - 1] : blocks[i - 1];
			Holder<Block> cur = blocks[i];
			Holder<Block> next = i == blocks.length - 1 ? blocks[0] : blocks[i + 1];
			register(cur, next, prev);
		}
		return this;
	}

	/**
	 * Registers world transmutations for all sequential blocks (including the last element to the first). Each registered transmutation will match any state and any
	 * properties that exist on both blocks will be transferred when transmuting.
	 *
	 * @param blocks List of blocks to register world transmutations for.
	 *
	 * @apiNote If this is called with only two elements, this will effectively just register two transmutations to convert one block into the other and one to convert
	 * the other back into the first one.
	 */
	public WorldTransmutationBuilder registerConsecutivePairs(Block... blocks) {
		if (blocks.length < 2) {
			throw new IllegalArgumentException("Expected at least two blocks for registering consecutive pairs");
		}
		for (int i = 0; i < blocks.length; i++) {
			Block prev = i == 0 ? blocks[blocks.length - 1] : blocks[i - 1];
			Block cur = blocks[i];
			Block next = i == blocks.length - 1 ? blocks[0] : blocks[i + 1];
			register(cur, next, prev);
		}
		return this;
	}

	/**
	 * Registers world transmutations for all sequential block states (including the last element to the first). Each registered transmutation will match only the exact
	 * state that was passed in.
	 *
	 * @param states List of block states to register world transmutations for.
	 *
	 * @apiNote If this is called with only two elements, this will effectively just register two transmutations to convert one block into the other and one to convert
	 * the other back into the first one.
	 */
	public WorldTransmutationBuilder registerConsecutivePairs(BlockState... states) {
		if (states.length < 2) {
			throw new IllegalArgumentException("Expected at least two states for registering consecutive pairs");
		}
		for (int i = 0; i < states.length; i++) {
			BlockState prev = i == 0 ? states[states.length - 1] : states[i - 1];
			BlockState cur = states[i];
			BlockState next = i == states.length - 1 ? states[0] : states[i + 1];
			register(cur, next, prev);
		}
		return this;
	}
}