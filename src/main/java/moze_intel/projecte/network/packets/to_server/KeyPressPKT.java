package moze_intel.projecte.network.packets.to_server;

import io.netty.buffer.ByteBuf;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import moze_intel.projecte.gameObjs.items.armor.GemChest;
import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import moze_intel.projecte.gameObjs.items.armor.GemHelmet;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record KeyPressPKT(PEKeybind key) implements IPEPacket {

	public static final CustomPacketPayload.Type<KeyPressPKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("key_press"));
	public static final StreamCodec<ByteBuf, KeyPressPKT> STREAM_CODEC = PEKeybind.STREAM_CODEC.map(KeyPressPKT::new, KeyPressPKT::key);

	@NotNull
	@Override
	public CustomPacketPayload.Type<KeyPressPKT> type() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext context) {
		Player player = context.player();
		if (player.isSpectator()) {
			return;
		}
		if (key == PEKeybind.HELMET_TOGGLE) {
			ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
			if (!helm.isEmpty() && helm.getItem() instanceof GemHelmet) {
				GemHelmet.toggleNightVision(helm, player);
			}
			return;
		} else if (key == PEKeybind.BOOTS_TOGGLE) {
			ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
			if (!boots.isEmpty() && boots.getItem() instanceof GemFeet feet) {
				feet.toggleStepAssist(boots, player);
			}
			return;
		}
		InternalAbilities internalAbilities = player.getData(PEAttachmentTypes.INTERNAL_ABILITIES);
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			switch (key) {
				case CHARGE -> {
					if (tryPerformCapability(player, stack, hand, PECapabilities.CHARGE_ITEM_CAPABILITY, IItemCharge::changeCharge)) {
						return;
					} else if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && GemArmorBase.hasAnyPiece(player)) {
						internalAbilities.toggleGemState();
						ILangEntry langEntry = internalAbilities.getGemState() ? PELang.GEM_ACTIVATE : PELang.GEM_DEACTIVATE;
						player.sendSystemMessage(langEntry.translate());
						return;
					}
				}
				case EXTRA_FUNCTION -> {
					if (tryPerformCapability(player, stack, hand, PECapabilities.EXTRA_FUNCTION_ITEM_CAPABILITY, IExtraFunction::doExtraFunction)) {
						return;
					} else if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && internalAbilities.getGemState()) {
						ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
						if (!chestplate.isEmpty() && chestplate.getItem() instanceof GemChest chest && internalAbilities.getGemCooldown() == 0) {
							chest.doExplode(player);
							internalAbilities.resetGemCooldown();
							return;
						}
					}
				}
				case FIRE_PROJECTILE -> {
					if (!stack.isEmpty() && internalAbilities.getProjectileCooldown() == 0 &&
						tryPerformCapability(player, stack, hand, PECapabilities.PROJECTILE_SHOOTER_ITEM_CAPABILITY, IProjectileShooter::shootProjectile)) {
						PlayerHelper.swingItem(player, hand);
						internalAbilities.resetProjectileCooldown();
					}
					if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && internalAbilities.getGemState()) {
						ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
						if (!helmet.isEmpty() && helmet.getItem() instanceof GemHelmet gemHelmet) {
							gemHelmet.doZap(player);
							return;
						}
					}
				}
				case MODE -> {
					if (tryPerformCapability(player, stack, hand, PECapabilities.MODE_CHANGER_ITEM_CAPABILITY, IModeChanger::changeMode)) {
						return;
					}
				}
			}
		}
	}

	private static <CAPABILITY> boolean tryPerformCapability(Player player, ItemStack stack, InteractionHand hand, ItemCapability<CAPABILITY, Void> capability,
			CapabilityProcessor<CAPABILITY> processor) {
		CAPABILITY impl = stack.getCapability(capability);
		return impl != null && processor.process(impl, player, stack, hand);
	}

	private static boolean isSafe(ItemStack stack) {
		return ProjectEConfig.server.misc.unsafeKeyBinds.get() || stack.isEmpty();
	}

	@FunctionalInterface
	private interface CapabilityProcessor<CAPABILITY> {

		boolean process(CAPABILITY capability, Player player, ItemStack stack, InteractionHand hand);
	}
}