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

	/**
	 * Codec for serializing and deserializing World Transmutations.
	 */
	public static final Codec<WorldTransmutation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			STATE_CODEC.fieldOf(ORIGIN_KEY).forGetter(WorldTransmutation::origin),
			STATE_CODEC.fieldOf(RESULT_KEY).forGetter(WorldTransmutation::result),
			STATE_CODEC.optionalFieldOf(ALT_RESULT_KEY).forGetter(entry -> entry.hasAlternate() ? Optional.of(entry.altResult()) : Optional.empty())
	).apply(instance, (origin, result, altResult) -> new WorldTransmutation(origin, result, altResult.orElse(result))));

	public WorldTransmutation {
		Objects.requireNonNull(origin, "Origin state cannot be null");
		Objects.requireNonNull(result, "Result state cannot be null");
		Objects.requireNonNull(altResult, "Alternate result state cannot be null");
	}

	/**
	 * @param origin defines what will match this transmutation.
	 * @param result defines what the normal right-click result of the transmutation will be.
	 */
	public WorldTransmutation(@NotNull BlockState origin, @NotNull BlockState result) {
		this(origin, result, result);
	}

	/**
	 * Creates a {@link WorldTransmutation} for the given states. If none of the states have any properties this will instead return a {@link SimpleWorldTransmutation}.
	 *
	 * @param origin defines what will match this transmutation.
	 * @param result defines what the normal right-click result of the transmutation will be.
	 */
	@NotNull
	public static IWorldTransmutation of(@NotNull BlockState origin, @NotNull BlockState result) {
		if (origin.getValues().isEmpty() && result.getValues().isEmpty()) {
			return new SimpleWorldTransmutation(origin.getBlock(), result.getBlock());
		}
		return new WorldTransmutation(origin, result);
	}

	/**
	 * Creates a {@link WorldTransmutation} for the given states. If none of the states have any properties this will instead return a {@link SimpleWorldTransmutation}.
	 *
	 * @param origin    defines what will match this transmutation.
	 * @param result    defines what the normal right-click result of the transmutation will be.
	 * @param altResult defines what the shift right-click result will be. May be equal to result.
	 */
	@NotNull
	public static IWorldTransmutation of(@NotNull BlockState origin, @NotNull BlockState result, @NotNull BlockState altResult) {
		if (origin.getValues().isEmpty() && result.getValues().isEmpty() && altResult.getValues().isEmpty()) {
			return new SimpleWorldTransmutation(origin.getBlock(), result.getBlock(), altResult.getBlock());
		}
		return new WorldTransmutation(origin, result, altResult);
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