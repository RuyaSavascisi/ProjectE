package moze_intel.projecte.impl;

import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.impl.capability.KnowledgeImpl.KnowledgeAttachment;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class TransmutationOffline {

	private static final IKnowledgeProvider NOT_FOUND_PROVIDER = immutableView(new KnowledgeAttachment());

	private static final Map<UUID, IKnowledgeProvider> cachedKnowledgeProviders = new HashMap<>();

	public static void cleanAll() {
		cachedKnowledgeProviders.clear();
	}

	public static void clear(UUID playerUUID) {
		cachedKnowledgeProviders.remove(playerUUID);
	}

	static IKnowledgeProvider forPlayer(MinecraftServer server, UUID playerUUID) {
		if (!cachedKnowledgeProviders.containsKey(playerUUID)) {
			if (!cacheOfflineData(server, playerUUID)) {
				cachedKnowledgeProviders.put(playerUUID, NOT_FOUND_PROVIDER);
			}
		}

		return cachedKnowledgeProviders.get(playerUUID);
	}

	private static boolean cacheOfflineData(MinecraftServer server, UUID playerUUID) {
		if (EffectiveSide.get().isClient()) {
			throw new IllegalStateException("CRITICAL: Trying to read filesystem on client!!");
		}
		Path player = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(playerUUID.toString() + ".dat");
		if (Files.exists(player) && Files.isRegularFile(player)) {
			try (InputStream in = Files.newInputStream(player)) {
				CompoundTag playerDat = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()); // No need to create buffered stream, that call does it for us
				if (playerDat.contains(AttachmentHolder.ATTACHMENTS_NBT_KEY, Tag.TAG_COMPOUND)) {
					CompoundTag attachmentData = playerDat.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY);
					CompoundTag knowledgeData = attachmentData.getCompound(PEAttachmentTypes.KNOWLEDGE.getId().toString());
					RegistryOps<Tag> serializationContext = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
					DataResult<KnowledgeAttachment> result = KnowledgeAttachment.CODEC.parse(serializationContext, knowledgeData);
					if (result.isSuccess()) {
						cachedKnowledgeProviders.put(playerUUID, immutableView(result.getOrThrow()));
						PECore.debugLog("Caching offline data for UUID: {}", playerUUID);
						return true;
					} else {
						result.ifError(error -> PECore.LOGGER.warn("Failed to cache offline data for API calls for UUID: {}. {}", playerUUID, error.message()));
					}
				}
			} catch (IOException e) {
				PECore.LOGGER.warn("Failed to cache offline data for API calls for UUID: {}", playerUUID);
			}
		}
		return false;
	}

	private static IKnowledgeProvider immutableView(final KnowledgeAttachment attachment) {
		final IKnowledgeProvider toCopy = KnowledgeImpl.wrapAttachment(attachment);
		return new IKnowledgeProvider() {
			final Set<ItemInfo> immutableKnowledge = Collections.unmodifiableSet(toCopy.getKnowledge());
			final IItemHandlerModifiable immutableInputLocks = ItemHelper.immutableCopy(toCopy.getInputAndLocks());

			@Override
			public boolean hasFullKnowledge() {
				return toCopy.hasFullKnowledge();
			}

			@Override
			public void setFullKnowledge(boolean fullKnowledge) {
			}

			@Override
			public void clearKnowledge() {
			}

			@Override
			public boolean hasKnowledge(@NotNull ItemInfo info) {
				return toCopy.hasKnowledge(info);
			}

			@Override
			public boolean addKnowledge(@NotNull ItemInfo info) {
				return false;
			}

			@Override
			public boolean removeKnowledge(@NotNull ItemInfo info) {
				return false;
			}

			@NotNull
			@Override
			public Set<ItemInfo> getKnowledge() {
				return immutableKnowledge;
			}

			@NotNull
			@Override
			public IItemHandler getInputAndLocks() {
				return immutableInputLocks;
			}

			@Override
			public BigInteger getEmc() {
				return toCopy.getEmc();
			}

			@Override
			public void setEmc(BigInteger emc) {
			}

			@Override
			public void sync(@NotNull ServerPlayer player) {
				toCopy.sync(player);
			}

			@Override
			public void syncEmc(@NotNull ServerPlayer player) {
				toCopy.syncEmc(player);
			}

			@Override
			public void syncKnowledgeChange(@NotNull ServerPlayer player, ItemInfo change, boolean learned) {
				toCopy.syncKnowledgeChange(player, change, learned);
			}

			@Override
			public void syncInputAndLocks(@NotNull ServerPlayer player, IntList slotsChanged, TargetUpdateType updateTargets) {
				toCopy.syncInputAndLocks(player, slotsChanged, updateTargets);
			}

			@Override
			public void receiveInputsAndLocks(Int2ObjectMap<ItemStack> changes) {
			}
		};
	}
}