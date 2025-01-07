package moze_intel.projecte.network;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.network.packets.to_client.NovaExplosionSyncPKT;
import moze_intel.projecte.network.packets.to_client.SyncBagDataPKT;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.network.packets.to_client.UpdateCondenserLockPKT;
import moze_intel.projecte.network.packets.to_client.UpdateWindowLongPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.network.packets.to_server.SearchUpdatePKT;
import moze_intel.projecte.network.packets.to_server.UpdateGemModePKT;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.maven.artifact.versioning.ArtifactVersion;

/**
 * Heavily based off of Mekanism's packet handler
 */
public final class PacketHandler {//TODO - 1.21: Potentially rename any packet ids that aren't that great

	//Client to server instanced packets
	private SimplePacketPayLoad leftClickArchangel;

	//Server to client instanced packets
	private SimplePacketPayLoad clearKnowledge;
	private SimplePacketPayLoad updateTransmutationTargets;

	private SimplePacketPayLoad resetCooldown;

	public PacketHandler(IEventBus modEventBus, ArtifactVersion version) {
		modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar(version.toString());
			registerClientToServer(new PacketRegistrar(registrar, true));
			registerServerToClient(new PacketRegistrar(registrar, false));
		});
	}

	private void registerClientToServer(PacketRegistrar registrar) {
		registrar.play(KeyPressPKT.TYPE, KeyPressPKT.STREAM_CODEC);
		leftClickArchangel = registrar.playInstanced(PECore.rl("left_click_archangel"), (ignored, context) -> {
			Player player = context.player();
			ItemStack main = player.getMainHandItem();
			if (!main.isEmpty() && main.getItem() instanceof ArchangelSmite archangelSmite) {
				archangelSmite.fireVolley(main, player);
			}
		});
		registrar.play(SearchUpdatePKT.TYPE, SearchUpdatePKT.STREAM_CODEC);
		registrar.play(UpdateGemModePKT.TYPE, UpdateGemModePKT.STREAM_CODEC);
	}

	private void registerServerToClient(PacketRegistrar registrar) {
		resetCooldown = registrar.playInstanced(PECore.rl("reset_cooldown"), (ignored, context) -> context.player().resetAttackStrengthTicker());
		clearKnowledge = registrar.playInstanced(PECore.rl("clear_knowledge"), (ignored, context) -> {
			Player player = context.player();
			IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (knowledge != null) {
				knowledge.clearKnowledge();
				if (player.containerMenu instanceof TransmutationContainer container) {
					container.transmutationInventory.updateClientTargets();
				}
			}
		});
		registrar.play(KnowledgeSyncPKT.TYPE, KnowledgeSyncPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncEmcPKT.TYPE, KnowledgeSyncEmcPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncInputsAndLocksPKT.TYPE, KnowledgeSyncInputsAndLocksPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncChangePKT.TYPE, KnowledgeSyncChangePKT.STREAM_CODEC);
		registrar.play(NovaExplosionSyncPKT.TYPE, NovaExplosionSyncPKT.STREAM_CODEC);
		registrar.play(SyncBagDataPKT.TYPE, SyncBagDataPKT.STREAM_CODEC);
		registrar.play(SyncEmcPKT.TYPE, SyncEmcPKT.STREAM_CODEC);
		registrar.play(SyncFuelMapperPKT.TYPE, SyncFuelMapperPKT.STREAM_CODEC);
		registrar.play(UpdateCondenserLockPKT.TYPE, UpdateCondenserLockPKT.STREAM_CODEC);
		updateTransmutationTargets = registrar.playInstanced(PECore.rl("update_transmutation_targets"), (ignored, context) -> {
			if (context.player().containerMenu instanceof TransmutationContainer container) {
				container.transmutationInventory.updateClientTargets();
			}
		});
		registrar.play(UpdateWindowLongPKT.TYPE, UpdateWindowLongPKT.STREAM_CODEC);
	}

	public void clearKnowledge(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, clearKnowledge);
	}

	public void updateTransmutationTargets(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, updateTransmutationTargets);
	}

	public void resetCooldown(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, resetCooldown);
	}

	public void leftClickArchangel() {
		PacketDistributor.sendToServer(leftClickArchangel);
	}

	protected record SimplePacketPayLoad(CustomPacketPayload.Type<CustomPacketPayload> type) implements CustomPacketPayload {

		private SimplePacketPayLoad(ResourceLocation id) {
			this(new CustomPacketPayload.Type<>(id));
		}
	}

	protected record PacketRegistrar(PayloadRegistrar registrar, boolean toServer) {

		public <MSG extends IPEPacket> void play(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> reader) {
			if (toServer) {
				registrar.playToServer(type, reader, IPEPacket::handle);
			} else {
				registrar.playToClient(type, reader, IPEPacket::handle);
			}
		}

		public SimplePacketPayLoad playInstanced(ResourceLocation id, IPayloadHandler<CustomPacketPayload> handler) {
			SimplePacketPayLoad payload = new SimplePacketPayLoad(id);
			if (toServer) {
				registrar.playToServer(payload.type(), StreamCodec.unit(payload), handler);
			} else {
				registrar.playToClient(payload.type(), StreamCodec.unit(payload), handler);
			}
			return payload;
		}
	}
}