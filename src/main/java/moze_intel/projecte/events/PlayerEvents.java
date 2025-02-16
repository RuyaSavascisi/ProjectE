package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.armor.PEArmor;
import moze_intel.projecte.gameObjs.items.armor.PEArmor.ReductionInfo;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

@EventBusSubscriber(modid = PECore.MODID)
public class PlayerEvents {

	// On death or return from end, sync to the client
	@SubscribeEvent
	public static void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (knowledge != null) {
				knowledge.sync(player);
			}
			IAlchBagProvider bagProvider = player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY);
			if (bagProvider != null) {
				bagProvider.syncAllBags(player);
			}
		}
	}

	@SubscribeEvent
	public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		Player player = event.getEntity();
		if (player instanceof ServerPlayer serverPlayer) {
			// Sync to the client for "normal" interdimensional teleports (nether portal, etc.)
			IKnowledgeProvider knowledge = serverPlayer.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (knowledge != null) {
				knowledge.sync(serverPlayer);
			}
			IAlchBagProvider bagProvider = serverPlayer.getCapability(PECapabilities.ALCH_BAG_CAPABILITY);
			if (bagProvider != null) {
				bagProvider.syncAllBags(serverPlayer);
			}
		}
	}

	@SubscribeEvent
	public static void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
		if (knowledge != null) {
			knowledge.sync(player);
			PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
		}

		IAlchBagProvider alchBagProvider = player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY);
		if (alchBagProvider != null) {
			alchBagProvider.syncAllBags(player);
		}

		PECore.debugLog("Sent knowledge and bag data to {}", player.getName());
	}

	@SubscribeEvent
	public static void onConstruct(EntityEvent.EntityConstructing evt) {
		if (EffectiveSide.get().isServer() // No world to check yet
			&& evt.getEntity() instanceof Player && !(evt.getEntity() instanceof FakePlayer)) {
			TransmutationOffline.clear(evt.getEntity().getUUID());
			PECore.debugLog("Clearing offline data cache in preparation to load online data");
		}
	}

	@SubscribeEvent
	public static void onHighAlchemistJoin(PlayerEvent.PlayerLoggedInEvent evt) {
		if (PECore.uuids.contains(evt.getEntity().getUUID().toString())) {
			MinecraftServer server = evt.getEntity().getServer();
			if (server != null) {
				Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, ChatFormatting.GOLD, evt.getEntity().getDisplayName());
				server.getPlayerList().broadcastSystemMessage(joinMessage, false);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void pickupItem(ItemEntityPickupEvent.Pre event) {
		ItemEntity itemEntity = event.getItemEntity();
		Player player = event.getPlayer();
		if (itemEntity.level().isClientSide || itemEntity.hasPickUpDelay() || itemEntity.getTarget() != null && !player.getUUID().equals(itemEntity.getTarget())) {
			return;
		}
		ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.getInventory().items);
		if (!bag.isEmpty()) {
			IAlchBagProvider bagProvider = player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY);
			if (bagProvider != null) {
				ItemStack stack = itemEntity.getItem();
				IItemHandler handler = bagProvider.getBag(((AlchemicalBag) bag.getItem()).color);
				ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stack, false);

				int pickedUpCount = stack.getCount() - remainder.getCount();
				if (pickedUpCount > 0) {
					event.setCanPickup(TriState.FALSE);
					player.take(itemEntity, pickedUpCount);
					if (remainder.isEmpty()) {
						itemEntity.discard();
						//Update to the picked up count so that onItemPickup knows how much got picked up
						stack.setCount(pickedUpCount);
					}
					player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), pickedUpCount);
					player.onItemPickup(itemEntity);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onInvulnerabilityChecked(EntityInvulnerabilityCheckEvent evt) {
		if (evt.getEntity() instanceof ServerPlayer player && evt.getSource().is(DamageTypeTags.IS_FIRE) && TickEvents.shouldPlayerResistFire(player)) {
			evt.setInvulnerable(true);
		}
	}

	//This event gets called when calculating how much damage to do to the entity, even if it is canceled the entity will still get "hit"
	@SubscribeEvent
	public static void onLivingDamaged(LivingIncomingDamageEvent event) {
		DamageContainer damageContainer = event.getContainer();
		if (damageContainer.getNewDamage() > 0) {
			ReductionInfo reductionInfo = ReductionInfo.ZERO;
			for (ItemStack armorStack : event.getEntity().getArmorSlots()) {
				if (armorStack.getItem() instanceof PEArmor armorItem) {
					//We return the max of this piece's base reduction (in relation to the full set),
					// and the max damage an item can absorb for a given source
					reductionInfo = reductionInfo.add(armorItem.getReductionInfo(damageContainer.getSource()));
				}
			}
			if (reductionInfo.percentReduced() >= 1) {
				event.setCanceled(true);
			} else if (reductionInfo.maxDamagedAbsorbed() > 0 && reductionInfo.percentReduced() > 0) {
				ReductionInfo info = reductionInfo;
				damageContainer.addModifier(Reduction.ARMOR, (container, reduction) -> {
					float damageAbsorbed = container.getNewDamage() * info.percentReduced();
					return reduction + Math.min(damageAbsorbed, info.maxDamagedAbsorbed());
				});
			}
		}
	}
}