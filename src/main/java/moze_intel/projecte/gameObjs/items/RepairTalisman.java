package moze_intel.projecte.gameObjs.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepairTalisman extends ItemPE implements IAlchBagItem, IAlchChestItem, IPedestalItem, ICapabilityAware {

	private static final BiPredicate<ItemStack, Void> CAN_REPAIR_ITEM = (stack, ignored) -> !stack.isEmpty() &&
																							stack.getCapability(PECapabilities.MODE_CHANGER_ITEM_CAPABILITY) == null &&
																							ItemHelper.isRepairableDamagedItem(stack);
	private static final BiPredicate<ItemStack, Player> CAN_REPAIR_PLAYER_ITEM =
			(stack, player) -> CAN_REPAIR_ITEM.test(stack, null) && (stack != player.getMainHandItem() || !player.swinging);

	public RepairTalisman(Properties props) {
		super(props.component(PEDataComponentTypes.COOLDOWN, (byte) 0));
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isHeld) {
		super.inventoryTick(stack, level, entity, slot, isHeld);
		if (!level.isClientSide && entity instanceof Player player && PlayerHelper.checkCooldown(player, this, ProjectEConfig.server.cooldown.player.repair)) {
			repairAllItems(player);
		}
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos,
			@NotNull PEDESTAL pedestal) {
		if (!level.isClientSide && ProjectEConfig.server.cooldown.pedestal.repair.get() != -1) {
			if (pedestal.getActivityCooldown() == 0) {
				level.getEntitiesOfClass(Player.class, pedestal.getEffectBounds()).forEach(RepairTalisman::repairAllItems);
				pedestal.setActivityCooldown(level, pos, ProjectEConfig.server.cooldown.pedestal.repair.get());
			} else {
				pedestal.decrementActivityCooldown(level, pos);
			}
		}
		return false;
	}

	@NotNull
	@Override
	public List<Component> getPedestalDescription(float tickRate) {
		List<Component> list = new ArrayList<>();
		if (ProjectEConfig.server.cooldown.pedestal.repair.get() != -1) {
			list.add(PELang.PEDESTAL_REPAIR_TALISMAN_1.translateColored(ChatFormatting.BLUE));
			list.add(PELang.PEDESTAL_REPAIR_TALISMAN_2.translateColored(ChatFormatting.BLUE, MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.repair.get(), tickRate)));
		}
		return list;
	}

	@Override
	public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
		if (!level.isClientSide) {
			IItemHandler inv = WorldHelper.getCapability(level, ItemHandler.BLOCK, pos, null);
			if (inv != null) {
				return updateInHandler(inv, stack);
			}
		}
		return false;
	}

	@Override
	public boolean updateInAlchBag(@NotNull IItemHandler inv, @NotNull Player player, @NotNull ItemStack stack) {
		return !player.level().isClientSide && updateInHandler(inv, stack);
	}

	private boolean updateInHandler(@NotNull IItemHandler inv, @NotNull ItemStack stack) {
		byte coolDown = stack.getOrDefault(PEDataComponentTypes.COOLDOWN, (byte) 0);
		if (coolDown > 0) {
			stack.set(PEDataComponentTypes.COOLDOWN, (byte) (coolDown - 1));
			return true;
		} else if (repairAllItems(inv, null, CAN_REPAIR_ITEM)) {
			stack.set(PEDataComponentTypes.COOLDOWN, (byte) 19);
			return true;
		}
		return false;
	}

	@Override
	public void attachCapabilities(RegisterCapabilitiesEvent event) {
		IntegrationHelper.registerCuriosCapability(event, this);
	}

	private static void repairAllItems(Player player) {
		repairAllItems(player.getCapability(ItemHandler.ENTITY), player, CAN_REPAIR_PLAYER_ITEM);
		repairAllItems(player.getCapability(IntegrationHelper.CURIO_ITEM_HANDLER), player, CAN_REPAIR_PLAYER_ITEM);
	}

	private static <DATA> boolean repairAllItems(@Nullable IItemHandler inv, DATA data, BiPredicate<ItemStack, DATA> canRepairStack) {
		if (inv == null) {
			return false;
		}
		boolean hasAction = false;
		for (int i = 0, slots = inv.getSlots(); i < slots; i++) {
			ItemStack invStack = inv.getStackInSlot(i);
			if (canRepairStack.test(invStack, data)) {
				invStack.setDamageValue(invStack.getDamageValue() - 1);
				if (!hasAction) {
					hasAction = true;
				}
			}
		}
		return hasAction;
	}
}