package moze_intel.projecte.emc;

import java.util.Comparator;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FuelMapper {

	private static HolderSet<Item> FUEL_MAP = HolderSet.empty();

	/**
	 * Used on server to load the map based on the tag
	 */
	public static void loadMap() {
		FUEL_MAP = HolderSet.direct(BuiltInRegistries.ITEM.getTag(PETags.Items.COLLECTOR_FUEL)
				.stream()
				.flatMap(HolderSet::stream)
				.filter(EMCHelper::doesItemHaveEmc)
				.sorted(Comparator.comparingLong(EMCHelper::getEmcValue))
				.toList());
	}

	/**
	 * Used on client side to set values from server
	 */
	public static void setFuelMap(HolderSet<Item> map) {
		FUEL_MAP = map;
	}

	public static SyncFuelMapperPKT getSyncPacket() {
		return new SyncFuelMapperPKT(FUEL_MAP);
	}

	public static boolean isStackFuel(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return FUEL_MAP.contains(stack.getItemHolder());
	}

	public static boolean isStackMaxFuel(ItemStack stack) {
		return stack.is(FUEL_MAP.get(FUEL_MAP.size() - 1));
	}

	public static ItemStack getFuelUpgrade(ItemStack stack) {
		for (int i = 0, elements = FUEL_MAP.size(); i < elements; i++) {
			if (stack.is(FUEL_MAP.get(i))) {
				//TODO - 1.21: Why does this return zero as the index for the last element
				int nextIndex = i + 1 == elements ? 0 : i + 1;
				return new ItemStack(FUEL_MAP.get(nextIndex));
			}
		}
		PECore.LOGGER.warn("Tried to upgrade invalid fuel: {}", stack.getItem());
		return ItemStack.EMPTY;
	}

	/**
	 * @return An immutable version of the Fuel Map
	 */
	public static HolderSet<Item> getFuelMap() {
		return FUEL_MAP;
	}
}