package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.impl.AlchBagImpl;
import moze_intel.projecte.impl.KnowledgeImpl;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.CheckUpdatePKT;
import moze_intel.projecte.network.packets.SyncCovalencePKT;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod.EventBusSubscriber(modid = PECore.MODID)
public class PlayerEvents
{
	// On death or return from end, copy the capability data
	@SubscribeEvent
	public static void cloneEvent(PlayerEvent.Clone evt)
	{
		evt.getOriginal().getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY).ifPresent(old -> {
			NBTTagCompound bags = old.serializeNBT();
			evt.getEntityPlayer().getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY).ifPresent(c -> c.deserializeNBT(bags));
		});

		evt.getOriginal().getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null).ifPresent(old -> {
			NBTTagCompound knowledge = old.serializeNBT();
			evt.getEntityPlayer().getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null).ifPresent(c -> c.deserializeNBT(knowledge));
		});
	}

	// On death or return from end, sync to the client
	@SubscribeEvent
	public static void respawnEvent(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent evt)
	{
		evt.getPlayer().getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.sync((EntityPlayerMP) evt.getPlayer()));
		evt.getPlayer().getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY).ifPresent(c -> c.sync(null, (EntityPlayerMP) evt.getPlayer()));
	}

	@SubscribeEvent
	public static void playerChangeDimension(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event)
	{
		// Sync to the client for "normal" interdimensional teleports (nether portal, etc.)
		event.getPlayer().getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY).ifPresent(c -> c.sync((EntityPlayerMP) event.getPlayer()));
		event.getPlayer().getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY, null).ifPresent(c -> c.sync(null, (EntityPlayerMP) event.getPlayer()));

		event.getPlayer().getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::onDimensionChange);
	}

	@SubscribeEvent
	public static void attachCaps(AttachCapabilitiesEvent<Entity> evt)
	{
		if (evt.getObject() instanceof EntityPlayer)
		{
			evt.addCapability(AlchBagImpl.Provider.NAME, new AlchBagImpl.Provider());
			evt.addCapability(KnowledgeImpl.Provider.NAME, new KnowledgeImpl.Provider((EntityPlayer) evt.getObject()));

			if (evt.getObject() instanceof EntityPlayerMP)
			{
				evt.addCapability(InternalTimers.NAME, new InternalTimers.Provider());
				evt.addCapability(InternalAbilities.NAME, new InternalAbilities.Provider((EntityPlayerMP) evt.getObject()));
			}
		}
	}

	@SubscribeEvent
	public static void playerConnect(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event)
	{
		EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
		PacketHandler.sendFragmentedEmcPacket(player);

		PacketHandler.sendTo(new CheckUpdatePKT(), player);

		player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
			knowledge.sync(player);
			PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, MathHelper.floor(knowledge.getEmc()));
		});

		player.getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY).ifPresent(c -> c.sync(null, player));

		PacketHandler.sendTo(new SyncCovalencePKT(ProjectEConfig.difficulty.covalenceLoss.get()), player);

		PECore.debugLog("Sent knowledge and bag data to {}", player.getName());
	}

	@SubscribeEvent
	public static void onConstruct(EntityEvent.EntityConstructing evt)
	{
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER // No world to check yet
			&& evt.getEntity() instanceof EntityPlayer && !(evt.getEntity() instanceof FakePlayer))
		{
			TransmutationOffline.clear(evt.getEntity().getUniqueID());
			PECore.debugLog("Clearing offline data cache in preparation to load online data");
		}
	}

	@SubscribeEvent
	public static void onHighAlchemistJoin(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent evt)
	{
		if (PECore.uuids.contains((evt.getPlayer().getUniqueID().toString())))
		{
			ITextComponent prior = new TextComponentTranslation("pe.server.high_alchemist").setStyle(new Style().setColor(TextFormatting.BLUE));
			ITextComponent playername = new TextComponentString(" " + evt.getPlayer().getName() + " ").setStyle(new Style().setColor(TextFormatting.GOLD));
			ITextComponent latter = new TextComponentTranslation("pe.server.has_joined").setStyle(new Style().setColor(TextFormatting.BLUE));
			ServerLifecycleHooks.getCurrentServer().getPlayerList().sendMessage(prior.appendSibling(playername).appendSibling(latter));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void pickupItem(EntityItemPickupEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();
		World world = player.getEntityWorld();
		
		if (world.isRemote)
		{
			return;
		}

		ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.inventory.mainInventory);

		if (bag.isEmpty())
		{
			return;
		}

		IItemHandler handler = player.getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY)
				.orElseThrow(NullPointerException::new)
				.getBag(((AlchemicalBag) bag.getItem()).color);
		ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, event.getItem().getItem(), false);

		if (remainder.isEmpty())
		{
			event.getItem().remove();
			world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			((EntityPlayerMP) player).connection.sendPacket(new SPacketCollectItem(event.getItem().getEntityId(), player.getEntityId(), 1));
		}
		else
		{
			event.getItem().setItem(remainder);
		}

		event.setCanceled(true);
	}
}
