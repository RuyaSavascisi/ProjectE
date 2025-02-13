package moze_intel.projecte.api.world_transmutation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.NotNull;

/**
 * @param origin    defines what will match this transmutation.
 * @param result    defines what the normal right-click result of the transmutation will be.
 * @param altResult defines what the shift right-click result will be. May be equal to result.
 */
public record WorldTransmutation(@NotNull BlockState origin, @NotNull BlockState result, @NotNull BlockState altResult) implements IWorldTransmutation {

	static final String ORIGIN_KEY = "origin";
	static final String RESULT_KEY = "result";
	static final String ALT_RESULT_KEY = "alt_result";

	static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();
	private static final Codec<BlockState> STATE_CODEC = NeoForgeExtraCodecs.withAlternative(BLOCK_CODEC.flatXmap(
			block -> DataResult.success(block.defaultBlockState()),
			state -> {
				if (state.getValues().isEmpty()) {
					return DataResult.success(state.getBlock());
				}
				return DataResult.error(() -> "Flattened state codec cannot be used for blocks that define any properties.");
			}
	), BlockState.CODEC);

	//TODO - 1.21: Do we want to just do blocks and then have the origin dispatch to properties to handle what outputs are created?
	// I think we might, but we probably want it as an alternative representation just so that we can provide people with the option to switch states to other ones
	// We even could maybe have the properties be listed as a diff compared to the default state for said block
	public static final Codec<WorldTransmutation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			STATE_CODEC.fieldOf(ORIGIN_KEY).forGetter(WorldTransmutation::origin),
			STATE_CODEC.fieldOf(RESULT_KEY).forGetter(WorldTransmutation::result),
			STATE_CODEC.optionalFieldOf(ALT_RESULT_KEY).forGetter(entry -> entry.hasAlternate() ? Optional.of(entry.altResult()) : Optional.empty())
	).apply(instance, (origin, result, altResult) -> new WorldTransmutation(origin, result, altResult.orElse(result))));

	public WorldTransmutation {
		Objects.requireNonNull(origin, "Origin state cannot be null");
		Objects.requireNonNull(result, "Result state cannot be null");
		Objects.requireNonNull(altResult, "Alternate result state cannot be null");
		//TODO - 1.21: Do we want to error if all states have no properties, as then they should be using a simple world transmutation?
		// If so do it via a helper maybe? And then make use of it for CrT
	}

	/**
	 * @param origin defines what will match this transmutation.
	 * @param result defines what the normal right-click result of the transmutation will be.
	 */
	public WorldTransmutation(BlockState origin, BlockState result) {
		this(origin, result, result);
	}


	@Override
	public boolean hasAlternate() {
		return altResult != result;
	}

	@Override
	public BlockState result(@NotNull BlockState state, boolean isSneaking) {
		if (canTransmute(state)) {
			return isSneaking ? altResult : result;
		}
		return null;
	}

	@Override
	public boolean canTransmute(@NotNull BlockState state) {
		return state == origin;
	}

	@Override
	public String toString() {
		String representation = "World Transmutation from: '" + origin + "' to: '" + result + "'";
		if (hasAlternate()) {
			representation += ", with secondary output of: '" + altResult + "'";
		}
		return representation;
	}
}