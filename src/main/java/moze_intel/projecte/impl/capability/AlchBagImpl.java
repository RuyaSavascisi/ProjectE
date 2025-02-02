package moze_intel.projecte.impl.capability;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.impl.codec.PECodecHelper;
import moze_intel.projecte.network.PEStreamCodecs;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncAllBagDataPKT;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncBagsDataPKT;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AlchBagImpl implements IAlchBagProvider {

	private final Player player;

	public AlchBagImpl(Player player) {
		this.player = player;
	}

	private AlchemicalBagAttachment attachment() {
		return this.player.getData(PEAttachmentTypes.ALCHEMICAL_BAGS);
	}

	@NotNull
	@Override
	public IItemHandler getBag(@NotNull DyeColor color) {
		return attachment().getBag(color);
	}

	@Override
	public void sync(@NotNull ServerPlayer player, @NotNull Set<DyeColor> colors) {
		if (!colors.isEmpty()) {
			AlchemicalBagAttachment attachment = attachment();
			Map<DyeColor, ItemStackHandler> handlers = new EnumMap<>(DyeColor.class);
			for (DyeColor color : colors) {
				handlers.put(color, attachment.getBag(color));
			}
			PacketDistributor.sendToPlayer(player, new SyncBagsDataPKT(handlers));
		}
	}

	@Override
	public void syncAllBags(@NotNull ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new SyncAllBagDataPKT(attachment()));
	}

	public static class AlchemicalBagAttachment {

		private static final int BAG_SIZE = 104;
		public static final Codec<AlchemicalBagAttachment> CODEC = Codec.unboundedMap(DyeColor.CODEC, PECodecHelper.MUTABLE_HANDLER_CODEC).xmap(
				map -> new AlchemicalBagAttachment(map.isEmpty() ? new EnumMap<>(DyeColor.class) : new EnumMap<>(map)),
				attachment -> attachment.inventories
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, Map<DyeColor, ItemStackHandler>> MAP_STREAM_CODEC = ByteBufCodecs.map(
				ignored -> new EnumMap<>(DyeColor.class),
				DyeColor.STREAM_CODEC,
				PEStreamCodecs.handlerStreamCodec(BAG_SIZE)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, AlchemicalBagAttachment> STREAM_CODEC = MAP_STREAM_CODEC.map(
				AlchemicalBagAttachment::new, attachment -> attachment.inventories
		);

		private final Map<DyeColor, ItemStackHandler> inventories;

		public AlchemicalBagAttachment(@Nullable IAttachmentHolder unused) {
			this(new EnumMap<>(DyeColor.class));
		}

		private AlchemicalBagAttachment(Map<DyeColor, ItemStackHandler> inventories) {
			this.inventories = inventories;
		}

		@Nullable
		public AlchemicalBagAttachment copy(IAttachmentHolder holder, HolderLookup.Provider registries) {
			AlchemicalBagAttachment copy = new AlchemicalBagAttachment(holder);
			for (Map.Entry<DyeColor, ItemStackHandler> entry : inventories.entrySet()) {
				copy.inventories.put(entry.getKey(), PEAttachmentTypes.copyHandler(entry.getValue(), ItemStackHandler::new));
			}
			return copy;
		}

		@NotNull
		public ItemStackHandler getBag(@NotNull DyeColor color) {
			return inventories.computeIfAbsent(color, c -> new ItemStackHandler(BAG_SIZE));
		}

		public void updateBags(Map<DyeColor, ItemStackHandler> handlers) {
			for (Map.Entry<DyeColor, ItemStackHandler> entry : handlers.entrySet()) {
				DyeColor color = entry.getKey();
				ItemStackHandler handler = entry.getValue();
				if (handler.getSlots() == BAG_SIZE) {
					inventories.put(color, handler);
				} else {
					PECore.LOGGER.warn("Received packet for updating {}, but the handler was of the wrong size. Expected: {}, Received: {}", color, BAG_SIZE, handler.getSlots());
				}
			}
		}
	}
}