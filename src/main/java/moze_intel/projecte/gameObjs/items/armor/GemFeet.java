package moze_intel.projecte.gameObjs.items.armor;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

public class GemFeet extends GemArmorBase implements IFlightProvider, IStepAssister {

	private static final Vec3 VERTICAL_MOVEMENT = new Vec3(0, 0.1, 0);
	private static final boolean STEP_ASSIST_DEFAULT = false;

	private final Supplier<ItemAttributeModifiers> defaultModifiers;

	public GemFeet(Properties props) {
		super(ArmorItem.Type.BOOTS, props.component(PEDataComponentTypes.STEP_ASSIST, STEP_ASSIST_DEFAULT));
		this.defaultModifiers = Suppliers.memoize(() -> super.getDefaultAttributeModifiers().withModifierAdded(
				Attributes.MOVEMENT_SPEED,
				new AttributeModifier(PECore.rl("armor"), 1.0, Operation.ADD_MULTIPLIED_TOTAL),
				EquipmentSlotGroup.FEET
		));
	}

	@NotNull
	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		return this.defaultModifiers.get();
	}

	public void toggleStepAssist(ItemStack boots, Player player) {
		//TODO - 1.21: Re-evaluate this, it seems wrong
		boolean oldValue = boots.getOrDefault(PEDataComponentTypes.STEP_ASSIST, STEP_ASSIST_DEFAULT);
		boots.set(PEDataComponentTypes.STEP_ASSIST, oldValue);
		if (oldValue) {
			player.sendSystemMessage(PELang.STEP_ASSIST.translate(ChatFormatting.RED, PELang.GEM_DISABLED));
		} else {
			player.sendSystemMessage(PELang.STEP_ASSIST.translate(ChatFormatting.GREEN, PELang.GEM_ENABLED));
		}
	}

	private static boolean isJumpPressed() {
		if (FMLEnvironment.dist.isClient()) {
			return Minecraft.getInstance().player != null && Minecraft.getInstance().player.input.jumping;
		}
		return false;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isHeld) {
		super.inventoryTick(stack, level, entity, slot, isHeld);
		if (isArmorSlot(slot) && entity instanceof Player player) {
			if (!level.isClientSide) {
				player.fallDistance = 0;
			} else {
				boolean flying = player.getAbilities().flying;
				if (!flying && isJumpPressed()) {
					player.addDeltaMovement(VERTICAL_MOVEMENT);
				}
				if (!player.onGround()) {
					if (player.getDeltaMovement().y() <= 0) {
						player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0.9, 1));
					}
					if (!flying) {
						if (player.zza < 0) {
							player.setDeltaMovement(player.getDeltaMovement().multiply(0.9, 1, 0.9));
						} else if (player.zza > 0 && player.getDeltaMovement().lengthSqr() < 3) {
							player.setDeltaMovement(player.getDeltaMovement().multiply(1.1, 1, 1.1));
						}
					}
				}
			}
		}
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(PELang.GEM_LORE_FEET.translate());
		tooltip.add(PELang.STEP_ASSIST_PROMPT.translate(ClientKeyHelper.getKeyName(PEKeybind.BOOTS_TOGGLE)));
		if (stack.getOrDefault(PEDataComponentTypes.STEP_ASSIST, STEP_ASSIST_DEFAULT)) {
			tooltip.add(PELang.STEP_ASSIST.translate(ChatFormatting.GREEN, PELang.GEM_ENABLED));
		} else {
			tooltip.add(PELang.STEP_ASSIST.translate(ChatFormatting.RED, PELang.GEM_DISABLED));
		}
	}

	@Override
	public boolean canProvideFlight(ItemStack stack, Player player) {
		return player.getItemBySlot(EquipmentSlot.FEET) == stack;
	}

	@Override
	public boolean canAssistStep(ItemStack stack, Player player) {
		return player.getItemBySlot(EquipmentSlot.FEET) == stack && stack.getOrDefault(PEDataComponentTypes.STEP_ASSIST, STEP_ASSIST_DEFAULT);
	}
}