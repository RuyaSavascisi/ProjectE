package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.armor.PEArmor;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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
				bagProvider.sync(null, player);
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
				bagProvider.sync(null, serverPlayer);
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
			alchBagProvider.sync(null, player);
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
			Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, ChatFormatting.GOLD, evt.getEntity().getDisplayName());
			ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(joinMessage, false);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void pickupItem(ItemEntityPickupEvent.Pre event) {
		Player player = event.getPlayer();
		Level level = player.level();
		if (level.isClientSide) {
			return;
		}
		ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.getInventory().items);
		if (bag.isEmpty()) {
			return;
		}
		IAlchBagProvider bagProvider = player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY);
		if (bagProvider == null) {
			return;
		}
		IItemHandler handler = bagProvider.getBag(((AlchemicalBag) bag.getItem()).color);
		ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, event.getItemEntity().getItem(), false);
		if (remainder.isEmpty()) {
			event.getItemEntity().discard();
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			((ServerPlayer) player).connection.send(new ClientboundTakeItemEntityPacket(event.getItemEntity().getId(), player.getId(), 1));
			//TODO - 1.21: Force allow the pickup? Though that doesn't change the fact vanilla tries to add it to the player's inventory
			//event.setCanPickup(TriState.TRUE);
		} else {
			event.getItemEntity().setItem(remainder);
		}
		//TODO - 1.21: Figure this out and if we should be using the pre or post event
		//event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onInvulnerabilityChecked(EntityInvulnerabilityCheckEvent evt) {
		if (evt.getEntity() instanceof ServerPlayer player && evt.getSource().is(DamageTypeTags.IS_FIRE) && TickEvents.shouldPlayerResistFire(player)) {
			evt.setInvulnerable(true);
		}
	}

	//This event gets called when calculating how much damage to do to the entity, even if it is canceled the entity will still get "hit"
	@SubscribeEvent
	public static void onLivingDamaged(LivingIncomingDamageEvent evt) {
		float damage = evt.getAmount();
		if (damage > 0) {
			//TODO - 1.21: Make use of the damage container and maybe apply this via a reduction modifier instead?
			LivingEntity entityLiving = evt.getEntity();
			DamageSource source = evt.getSource();
			float totalPercentReduced = getReductionForSlot(entityLiving, source, EquipmentSlot.HEAD, damage) +
										getReductionForSlot(entityLiving, source, EquipmentSlot.CHEST, damage) +
										getReductionForSlot(entityLiving, source, EquipmentSlot.LEGS, damage) +
										getReductionForSlot(entityLiving, source, EquipmentSlot.FEET, damage);
			float damageAfter = totalPercentReduced >= 1 ? 0 : damage - damage * totalPercentReduced;
			if (damageAfter <= 0) {
				evt.setCanceled(true);
			} else if (damage != damageAfter) {
				evt.setAmount(damageAfter);
			}
		}
	}

	private static float getReductionForSlot(LivingEntity entityLiving, DamageSource source, EquipmentSlot slot, float damage) {
		ItemStack armorStack = entityLiving.getItemBySlot(slot);
		if (armorStack.getItem() instanceof PEArmor armorItem) {
			ArmorItem.Type type = armorItem.getType();
			if (type.getSlot() != slot) {
				//If the armor slot does not match the slot this piece of armor is for then it shouldn't be providing any reduction
				return 0;
			}
			//We return the max of this piece's base reduction (in relation to the full set), and the
			// max damage an item can absorb for a given source
			return Math.max(armorItem.getFullSetBaseReduction(), armorItem.getMaxDamageAbsorb(type, source) / damage) * armorItem.getPieceEffectiveness(type);
		}
		return 0;
	}
}