package moze_intel.projecte.network.packets.to_client;

import io.netty.handler.codec.EncoderException;
import java.util.HashSet;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.world_transmutation.IWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.SimpleWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.WorldTransmutation;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.world_transmutation.WorldTransmutationManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncWorldTransmutations(Set<IWorldTransmutation> transmutations) implements IPEPacket {

	public static final Type<SyncWorldTransmutations> TYPE = new Type<>(PECore.rl("sync_world_transmutations"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncWorldTransmutations> STREAM_CODEC =
			ByteBufCodecs.<RegistryFriendlyByteBuf, IWorldTransmutation, Set<IWorldTransmutation>>collection(HashSet::new, new StreamCodec<>() {
				@Override
				public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull IWorldTransmutation value) {
					switch (value) {
						case SimpleWorldTransmutation transmutation -> {
							buffer.writeBoolean(false);
							SimpleWorldTransmutation.STREAM_CODEC.encode(buffer, transmutation);
						}
						case WorldTransmutation transmutation -> {
							buffer.writeBoolean(true);
							WorldTransmutation.STREAM_CODEC.encode(buffer, transmutation);
						}
						default -> throw new EncoderException("Unknown world transmutation implementation: " + value);
					}
				}

				@NotNull
				@Override
				public IWorldTransmutation decode(@NotNull RegistryFriendlyByteBuf buffer) {
					if (buffer.readBoolean()) {
						return WorldTransmutation.STREAM_CODEC.decode(buffer);
					}
					return SimpleWorldTransmutation.STREAM_CODEC.decode(buffer);
				}
			}).map(SyncWorldTransmutations::new, SyncWorldTransmutations::transmutations);

	@NotNull
	@Override
	public CustomPacketPayload.Type<SyncWorldTransmutations> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		PECore.debugLog("Receiving World Transmutation data from server.");
		WorldTransmutationManager.INSTANCE.setEntries(transmutations);
	}
}