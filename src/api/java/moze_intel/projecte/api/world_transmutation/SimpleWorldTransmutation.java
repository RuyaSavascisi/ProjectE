package moze_intel.projecte.api.world_transmutation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a simple world transmutation that goes from one block to another and copies any state properties that exist for both the origin and the result.
 *
 * @param origin    defines what will match this transmutation.
 * @param result    defines what the normal right-click result of the transmutation will be.
 * @param altResult defines what the shift right-click result will be. May be equal to result.
 */
public record SimpleWorldTransmutation(@NotNull Block origin, @NotNull Block result, @NotNull Block altResult) implements IWorldTransmutation {

	//TODO - 1.21: Should we convert simple world transmutation to using holders?
	private static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();
	private static final StreamCodec<RegistryFriendlyByteBuf, Block> BLOCK_STREAM_CODEC = ByteBufCodecs.registry(Registries.BLOCK);
	/**
	 * Codec for serializing and deserializing simple World Transmutations.
	 */
	public static final Codec<SimpleWorldTransmutation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BLOCK_CODEC.fieldOf(WorldTransmutation.ORIGIN_KEY).forGetter(SimpleWorldTransmutation::origin),
			BLOCK_CODEC.fieldOf(WorldTransmutation.RESULT_KEY).forGetter(SimpleWorldTransmutation::result),
			BLOCK_CODEC.optionalFieldOf(WorldTransmutation.ALT_RESULT_KEY).forGetter(entry -> entry.hasAlternate() ? Optional.of(entry.altResult()) : Optional.empty())
	).apply(instance, (origin, result, altResult) -> new SimpleWorldTransmutation(origin, result, altResult.orElse(result))));
	/**
	 * Stream codec for serializing and deserializing simple World Transmutations over the network.
	 */
	public static final StreamCodec<RegistryFriendlyByteBuf, SimpleWorldTransmutation> STREAM_CODEC = new StreamCodec<>() {
		@NotNull
		@Override
		public SimpleWorldTransmutation decode(@NotNull RegistryFriendlyByteBuf buffer) {
			Block origin = BLOCK_STREAM_CODEC.decode(buffer);
			Block result = BLOCK_STREAM_CODEC.decode(buffer);
			if (buffer.readBoolean()) {
				return new SimpleWorldTransmutation(origin, result, BLOCK_STREAM_CODEC.decode(buffer));
			}
			return new SimpleWorldTransmutation(origin, result);
		}

		@Override
		public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull SimpleWorldTransmutation transmutation) {
			BLOCK_STREAM_CODEC.encode(buffer, transmutation.origin());
			BLOCK_STREAM_CODEC.encode(buffer, transmutation.result());
			boolean hasAlternate = transmutation.hasAlternate();
			buffer.writeBoolean(hasAlternate);
			if (hasAlternate) {
				BLOCK_STREAM_CODEC.encode(buffer, transmutation.altResult());
			}
		}
	};

	public SimpleWorldTransmutation {
		Objects.requireNonNull(origin, "Origin state cannot be null");
		Objects.requireNonNull(result, "Result state cannot be null");
		Objects.requireNonNull(altResult, "Alternate result state cannot be null");
	}

	/**
	 * @param origin defines what will match this transmutation.
	 * @param result defines what the normal right-click result of the transmutation will be.
	 */
	public SimpleWorldTransmutation(@NotNull Block origin, @NotNull Block result) {
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