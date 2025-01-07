package moze_intel.projecte.network.packets.to_client;

import io.netty.buffer.ByteBuf;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncBagDataPKT(CompoundTag nbt) implements IPEPacket {

	public static final CustomPacketPayload.Type<SyncBagDataPKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("sync_bag_data"));
	public static final StreamCodec<ByteBuf, SyncBagDataPKT> STREAM_CODEC = ByteBufCodecs.TRUSTED_COMPOUND_TAG.map(SyncBagDataPKT::new, SyncBagDataPKT::nbt);

	@NotNull
	@Override
	public CustomPacketPayload.Type<SyncBagDataPKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		//We have to use the client's player instance rather than context#player as the first usage of this packet is sent during player login
		// which is before the player exists on the client so the context does not contain it.
		//Note: This must stay LocalPlayer to not cause classloading issues
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			player.getData(PEAttachmentTypes.ALCHEMICAL_BAGS).deserializeNBT(player.registryAccess(), nbt);
		}
		PECore.debugLog("** RECEIVED BAGS CLIENTSIDE **");
	}
}