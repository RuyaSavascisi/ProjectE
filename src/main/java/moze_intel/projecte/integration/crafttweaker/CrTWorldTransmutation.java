package moze_intel.projecte.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import moze_intel.projecte.api.world_transmutation.IWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.SimpleWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.WorldTransmutation;
import moze_intel.projecte.integration.crafttweaker.actions.WorldTransmuteAction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@Document("mods/ProjectE/WorldTransmutation")
@ZenCodeType.Name("mods.projecte.WorldTransmutation")
public class CrTWorldTransmutation {

	private CrTWorldTransmutation() {
	}

	/**
	 * Adds a simple in world transmutation "recipe" that handles state data as best as it can.
	 *
	 * @param input           {@link BlockState} representing the input or target.
	 * @param output          {@link BlockState} representing the output.
	 * @param secondaryOutput Optional {@link BlockState} representing the output when sneaking.
	 */
	@ZenCodeType.Method
	public static void add(Block input, Block output, @ZenCodeType.Optional Block secondaryOutput) {
		IWorldTransmutation transmutation = new SimpleWorldTransmutation(input, output, secondaryOutput == null ? output : secondaryOutput);
		CraftTweakerAPI.apply(new WorldTransmuteAction(transmutation, true));
	}

	/**
	 * Adds an in world transmutation "recipe" for specific block states.
	 *
	 * @param input           {@link BlockState} representing the input or target state.
	 * @param output          {@link BlockState} representing the output state.
	 * @param secondaryOutput Optional {@link BlockState} representing the output state when sneaking.
	 */
	@ZenCodeType.Method
	public static void add(BlockState input, BlockState output, @ZenCodeType.Optional BlockState secondaryOutput) {
		IWorldTransmutation transmutation = WorldTransmutation.of(input, output, secondaryOutput == null ? output : secondaryOutput);
		CraftTweakerAPI.apply(new WorldTransmuteAction(transmutation, true));
	}

	/**
	 * Removes an existing simple in world transmutation "recipe" that handles state data as best as it can.
	 *
	 * @param input           {@link BlockState} representing the input or target.
	 * @param output          {@link BlockState} representing the output.
	 * @param secondaryOutput Optional {@link BlockState} representing the output when sneaking.
	 */
	@ZenCodeType.Method
	public static void remove(Block input, Block output, @ZenCodeType.Optional Block secondaryOutput) {
		IWorldTransmutation transmutation = new SimpleWorldTransmutation(input, output, secondaryOutput == null ? output : secondaryOutput);
		CraftTweakerAPI.apply(new WorldTransmuteAction(transmutation, false));
	}

	/**
	 * Removes an existing in world transmutation "recipe" for specific block states.
	 *
	 * @param input           {@link BlockState} representing the input or target state.
	 * @param output          {@link BlockState} representing the output state.
	 * @param secondaryOutput Optional {@link BlockState} representing the output state when sneaking.
	 */
	@ZenCodeType.Method
	public static void remove(BlockState input, BlockState output, @ZenCodeType.Optional BlockState secondaryOutput) {
		IWorldTransmutation transmutation = WorldTransmutation.of(input, output, secondaryOutput == null ? output : secondaryOutput);
		CraftTweakerAPI.apply(new WorldTransmuteAction(transmutation, false));
	}

	/**
	 * Removes all existing in world transmutation "recipes".
	 */
	@ZenCodeType.Method
	public static void removeAll() {
		CraftTweakerAPI.apply(new WorldTransmuteAction.RemoveAll());
	}
}