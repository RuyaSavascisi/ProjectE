package moze_intel.projecte.network.packets.to_client;

import io.netty.buffer.Unpooled;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncEmcPKT(EmcPKTInfo[] data) implements IPEPacket {

	public static final CustomPacketPayload.Type<SyncEmcPKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("sync_emc"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncEmcPKT> STREAM_CODEC = StreamCodec.of((buffer, pkt) -> {
				buffer.writeVarInt(pkt.data().length);
				for (EmcPKTInfo info : pkt.data()) {
					EmcPKTInfo.STREAM_CODEC.encode(buffer, info);
				}
			}, (buffer) -> {
				EmcPKTInfo[] data = new EmcPKTInfo[buffer.readVarInt()];
				for (int i = 0; i < data.length; i++) {
					data[i] = EmcPKTInfo.STREAM_CODEC.decode(buffer);
				}
				return new SyncEmcPKT(data);
			}
	);

	public SyncEmcPKT(RegistryAccess registryAccess) {
		this(serializeEmcData(registryAccess));
	}

	@NotNull
	@Override
	public CustomPacketPayload.Type<SyncEmcPKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		PECore.LOGGER.info("Receiving EMC data from server.");
		EMCMappingHandler.fromPacket(data);
	}

	public static EmcPKTInfo[] serializeEmcData(RegistryAccess registryAccess) {
		EmcPKTInfo[] data = EMCMappingHandler.createPacketData();
		//Simulate encoding the EMC packet to get an accurate size
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess, ConnectionType.NEOFORGE);
		try {
			int index = buf.writerIndex();
			SyncEmcPKT.STREAM_CODEC.encode(buf, new SyncEmcPKT(data));
			PECore.debugLog("EMC data size: {} bytes", buf.writerIndex() - index);
		} finally {
			buf.release();
		}
		return data;
	}

	public record EmcPKTInfo(ItemInfo item, long emc) {

		private static final StreamCodec<RegistryFriendlyByteBuf, EmcPKTInfo> STREAM_CODEC = StreamCodec.composite(
				ItemInfo.STREAM_CODEC, EmcPKTInfo::item,
				ByteBufCodecs.VAR_LONG, EmcPKTInfo::emc,
				EmcPKTInfo::new
		);
	}
}