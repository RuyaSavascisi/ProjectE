package moze_intel.projecte.network.packets.to_server;

import io.netty.buffer.ByteBuf;
import moze_intel.projecte.PECore;
import moze_intel.projecte.components.GemData;
import moze_intel.projecte.gameObjs.items.GemEternalDensity;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.network.packets.IPEPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateGemModePKT(boolean mode) implements IPEPacket {

	public static final CustomPacketPayload.Type<UpdateGemModePKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("update_gem_mode"));
	public static final StreamCodec<ByteBuf, UpdateGemModePKT> STREAM_CODEC = ByteBufCodecs.BOOL.map(UpdateGemModePKT::new, UpdateGemModePKT::mode);

	@NotNull
	@Override
	public CustomPacketPayload.Type<UpdateGemModePKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		Player player = context.player();
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			stack = player.getOffhandItem();
		}
		//Note: Void Ring extends gem of eternal density, so we only need to check if it is an instance of the base class
		if (!stack.isEmpty() && stack.getItem() instanceof GemEternalDensity) {
			stack.update(PEDataComponentTypes.GEM_DATA, GemData.EMPTY, mode, GemData::withWhitelist);
		}
	}
}