package moze_intel.projecte.gameObjs.items;

import java.util.Objects;
import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

public class ItemPE extends Item {

	public ItemPE(Properties props) {
		super(props);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (oldStack.getItem() != newStack.getItem()) {
			return true;
		} else if (oldStack.getOrDefault(PEDataComponentTypes.ACTIVE, false) != newStack.getOrDefault(PEDataComponentTypes.ACTIVE, false)) {
			return true;
		}
		return this instanceof IModeChanger<?> modeChanger && !modeMatches(modeChanger, oldStack, newStack);
	}

	private static <MODE> boolean modeMatches(IModeChanger<MODE> modeChanger, ItemStack oldStack, ItemStack newStack) {
		return Objects.equals(modeChanger.getMode(oldStack), modeChanger.getMode(newStack));
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmc(ItemStack stack) {
		return stack.getOrDefault(PEDataComponentTypes.STORED_EMC, 0L);
	}

	public static void addEmcToStack(ItemStack stack, @Range(from = 0, to = Long.MAX_VALUE) long amount) {
		if (amount > 0) {
			stack.update(PEDataComponentTypes.STORED_EMC, 0L, amount, Long::sum);
		}
	}

	public static void removeEmc(ItemStack stack, @Range(from = 0, to = Long.MAX_VALUE) long amount) {
		if (amount > 0) {
			stack.update(PEDataComponentTypes.STORED_EMC, 0L, amount, (emc, change) -> Math.max(emc - change, 0));
		}
	}

	public static boolean consumeFuel(Player player, ItemStack stack, long amount, boolean shouldRemove) {
		if (amount <= 0) {
			return true;
		}
		long current = getEmc(stack);
		if (current < amount) {
			long consume = EMCHelper.consumePlayerFuel(player, amount - current);
			if (consume == -1) {
				return false;
			}
			addEmcToStack(stack, consume);
		}
		if (shouldRemove) {
			removeEmc(stack, amount);
		}
		return true;
	}

	public static boolean hotBarOrOffHand(int slot) {
		return slot < Inventory.getSelectionSize() || slot == Inventory.SLOT_OFFHAND;
	}
}