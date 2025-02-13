package moze_intel.projecte.api.world_transmutation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.21: Implement docs and stuff for this
public record SimpleWorldTransmutation(@NotNull Block origin, @NotNull Block result, @NotNull Block altResult) implements IWorldTransmutation {

	public static final Codec<SimpleWorldTransmutation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			WorldTransmutation.BLOCK_CODEC.fieldOf(WorldTransmutation.ORIGIN_KEY).forGetter(SimpleWorldTransmutation::origin),
			WorldTransmutation.BLOCK_CODEC.fieldOf(WorldTransmutation.RESULT_KEY).forGetter(SimpleWorldTransmutation::result),
			WorldTransmutation.BLOCK_CODEC.optionalFieldOf(WorldTransmutation.ALT_RESULT_KEY).forGetter(entry -> entry.hasAlternate() ? Optional.of(entry.altResult()) : Optional.empty())
	).apply(instance, (origin, result, altResult) -> new SimpleWorldTransmutation(origin, result, altResult.orElse(result))));

	public SimpleWorldTransmutation {
		Objects.requireNonNull(origin, "Origin state cannot be null");
		Objects.requireNonNull(result, "Result state cannot be null");
		Objects.requireNonNull(altResult, "Alternate result state cannot be null");
	}

	/**
	 * @param origin defines what will match this transmutation.
	 * @param result defines what the normal right-click result of the transmutation will be.
	 */
	public SimpleWorldTransmutation(Block origin, Block result) {
		this(origin, result, result);
	}

	@Override
	public boolean hasAlternate() {
		return result != altResult;
	}

	@Nullable
	@Override
	public BlockState result(@NotNull BlockState state, boolean isSneaking) {
		if (canTransmute(state)) {
			Block resultBlock = isSneaking ? altResult : result;
			return resultBlock.withPropertiesOf(state);
		}
		return null;
	}

	@Override
	public boolean canTransmute(@NotNull BlockState state) {
		return state.is(origin);
	}

	@Override
	public String toString() {
		String representation = "Simple World Transmutation from: " + Util.getRegisteredName(BuiltInRegistries.BLOCK, origin)
								+ " to: " + Util.getRegisteredName(BuiltInRegistries.BLOCK, result);
		if (hasAlternate()) {
			representation += ", with secondary output of: " + Util.getRegisteredName(BuiltInRegistries.BLOCK, altResult);
		}
		return representation;
	}
}