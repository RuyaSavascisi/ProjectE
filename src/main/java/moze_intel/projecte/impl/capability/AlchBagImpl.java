package moze_intel.projecte.impl.capability;

import java.util.EnumMap;
import java.util.Map;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.network.packets.to_client.SyncBagDataPKT;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
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
	public void sync(@Nullable DyeColor color, @NotNull ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new SyncBagDataPKT(attachment().writeNBT(player.registryAccess(), color)));
	}

	public static class AlchemicalBagAttachment implements INBTSerializable<CompoundTag> {

		private final Map<DyeColor, ItemStackHandler> inventories = new EnumMap<>(DyeColor.class);

		@Nullable
		public AlchemicalBagAttachment copy(IAttachmentHolder holder, HolderLookup.Provider registries) {
			AlchemicalBagAttachment copy = new AlchemicalBagAttachment();
			inventories.forEach((color, handler) -> copy.inventories.put(color, PEAttachmentTypes.copyHandler(handler, ItemStackHandler::new)));
			return copy;
		}

		@NotNull
		public IItemHandlerModifiable getBag(@NotNull DyeColor color) {
			return inventories.computeIfAbsent(color, c -> new ItemStackHandler(104));
		}

		private CompoundTag writeNBT(HolderLookup.Provider registries, DyeColor color) {
			CompoundTag ret = new CompoundTag();
			DyeColor[] colors = color == null ? DyeColor.values() : new DyeColor[]{color};
			for (DyeColor c : colors) {
				ItemStackHandler handler = inventories.get(c);
				if (handler != null) {
					ret.put(c.getSerializedName(), handler.serializeNBT(registries));
				}
			}
			return ret;
		}

		@Override
		public CompoundTag serializeNBT(HolderLookup.Provider registries) {
			return writeNBT(registries, null);
		}

		@Override
		public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
			for (DyeColor e : DyeColor.values()) {
				if (nbt.contains(e.getSerializedName(), Tag.TAG_COMPOUND)) {
					ItemStackHandler inv = new ItemStackHandler(104);
					inv.deserializeNBT(registries, nbt.getCompound(e.getSerializedName()));
					inventories.put(e, inv);
				}
			}
		}
	}
}