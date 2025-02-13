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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

//TODO - 1.21: Docs for all the methods
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

	public WorldTransmutationBuilder register(BlockState from, BlockState result) {
		if (!transmutationEntries.add(new WorldTransmutation(from, result))) {
			throw new IllegalStateException("World transmutation file contains duplicate transmutations.");
		}
		return this;
	}

	public WorldTransmutationBuilder register(BlockState from, BlockState result, BlockState altResult) {
		//TODO - 1.21: Prevent against duplicate input blocks as well?
		if (!transmutationEntries.add(new WorldTransmutation(from, result, altResult))) {
			throw new IllegalStateException("World transmutation file contains duplicate transmutations.");
		}
		return this;
	}

	public WorldTransmutationBuilder register(Block from, Block result) {
		if (!transmutationEntries.add(new SimpleWorldTransmutation(from, result))) {
			throw new IllegalStateException("World transmutation file contains duplicate transmutations.");
		}
		return this;
	}

	public WorldTransmutationBuilder register(Block from, Block result, Block altResult) {
		//TODO - 1.21: Prevent against duplicate input blocks as well?
		if (!transmutationEntries.add(new SimpleWorldTransmutation(from, result, altResult))) {
			throw new IllegalStateException("World transmutation file contains duplicate transmutations.");
		}
		return this;
	}

	public WorldTransmutationBuilder registerBiDirectional(Block first, Block second) {
		return registerConsecutivePairs(first, second);
	}

	public WorldTransmutationBuilder registerBiDirectional(BlockState first, BlockState second) {
		return registerConsecutivePairs(first, second);
	}

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