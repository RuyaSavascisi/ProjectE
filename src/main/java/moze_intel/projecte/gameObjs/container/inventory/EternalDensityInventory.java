package moze_intel.projecte.gameObjs.container.inventory;

import java.util.List;
import moze_intel.projecte.components.GemData;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.network.packets.to_server.UpdateGemModePKT;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class EternalDensityInventory implements IItemHandlerModifiable {

	private final NonNullList<ItemStack> inventoryStacks = NonNullList.withSize(9, ItemStack.EMPTY);
	private final ItemStackHandler inventory = new ItemStackHandler(inventoryStacks);
	public final ItemStack invItem;
	private GemData gemData;

	public EternalDensityInventory(ItemStack stack) {
		this.invItem = stack;
		gemData = stack.getOrDefault(PEDataComponentTypes.GEM_DATA, GemData.EMPTY);
		for (int i = 0, size = Math.min(gemData.whitelist().size(), 9); i < size; i++) {
			inventoryStacks.set(i, gemData.whitelist().get(i).copy());
		}
	}

	@Override
	public int getSlots() {
		return inventory.getSlots();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory.getStackInSlot(slot);
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		ItemStack ret = inventory.insertItem(slot, stack, simulate);
		writeBack();
		return ret;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack ret = inventory.extractItem(slot, amount, simulate);
		writeBack();
		return ret;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return inventory.isItemValid(slot, stack);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		inventory.setStackInSlot(slot, stack);
		writeBack();
	}

	//TODO - 1.21: Test this
	private void writeBack() {
		//TODO: I think we can remove this
		/*for (int i = 0; i < inventory.getSlots(); ++i) {
			if (inventory.getStackInSlot(i).isEmpty()) {
				inventory.setStackInSlot(i, ItemStack.EMPTY);
			}
		}*/
		//Create a list that will be the correct size
		int size = inventoryStacks.size();
		List<ItemStack> targets = NonNullList.withSize(size, ItemStack.EMPTY);
		for (int i = 0; i < size; i++) {
			//Get the item in the slot
			ItemStack target = inventoryStacks.get(i);
			//If it isn't empty and none of the
			if (noneMatch(targets, target)) {
				targets.set(i, target.copy());
			}
		}
		gemData = gemData.withWhitelist(targets);
		invItem.set(PEDataComponentTypes.GEM_DATA, gemData);
	}

	private boolean noneMatch(List<ItemStack> targets, @NotNull ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		for (ItemStack s : targets) {
			if (ItemStack.isSameItemSameComponents(s, stack)) {
				return false;
			}
		}
		return true;
	}

	public void changeMode() {
		gemData = gemData.toggleWhitelist();
		invItem.set(PEDataComponentTypes.GEM_DATA, gemData);
		//TODO - 1.21: Do we actually need to do a writeBack here or are the targets fine as is?
		//invItem.update(PEDataComponentTypes.GEM_DATA, gemData, GemData::toggleWhitelist);
		writeBack();
		PacketDistributor.sendToServer(new UpdateGemModePKT(gemData.isWhitelist()));
	}

	public boolean isWhitelistMode() {
		return gemData.isWhitelist();
	}
}