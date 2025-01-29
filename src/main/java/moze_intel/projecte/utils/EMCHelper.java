package moze_intel.projecte.utils;

import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.math.BigInteger;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage.EmcAction;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.emc.components.DataComponentManager;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Helper class for EMC. Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class EMCHelper {

	public static <K> Object2IntMap<K> intMapOf(final K key, int value, final K key2, int value2) {
		Object2IntMap<K> intMap = new Object2IntArrayMap<>(2);
		intMap.put(key, value);
		intMap.put(key2, value2);
		return intMap;
	}

	public static <K> Object2IntMap<K> intMapOf(final K key, int value, final K key2, int value2, final K key3, int value3) {
		Object2IntMap<K> intMap = new Object2IntArrayMap<>(3);
		intMap.put(key, value);
		intMap.put(key2, value2);
		intMap.put(key3, value3);
		return intMap;
	}

	/**
	 * Consumes EMC from fuel items or Klein Stars Any extra EMC is discarded !!! To retain remainder EMC use ItemPE.consumeFuel()
	 *
	 * @implNote Order it tries to extract from is, Curios, Offhand, main inventory
	 */
	public static long consumePlayerFuel(Player player, @Range(from = 0, to = Long.MAX_VALUE) long minFuel) {
		if (player.isCreative() || minFuel == 0) {
			return minFuel;
		}
		IItemHandler curios = player.getCapability(IntegrationHelper.CURIO_ITEM_HANDLER);
		if (curios != null) {
			for (int i = 0, slots = curios.getSlots(); i < slots; i++) {
				long actualExtracted = tryExtract(curios.getStackInSlot(i), minFuel);
				if (actualExtracted > 0) {
					player.containerMenu.broadcastChanges();
					return actualExtracted;
				}
			}
		}

		//Note: The implementation of this will iterate in the order: Main inventory, Armor, Offhand
		IItemHandler inv = player.getCapability(ItemHandler.ENTITY);
		if (inv != null) {
			//Ensure that we have an item handler capability, because if for example the player is dead we will not
			//TODO - 1.21: Does this actually need to be linked?
			Int2IntMap map = new Int2IntLinkedOpenHashMap();
			boolean metRequirement = false;
			long emcConsumed = 0;
			for (int i = 0, slots = inv.getSlots(); i < slots; i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.isEmpty()) {
					continue;
				}
				long actualExtracted = tryExtract(stack, minFuel);
				if (actualExtracted > 0) { //Prioritize extracting from emc storage items
					player.containerMenu.broadcastChanges();
					return actualExtracted;
				} else if (!metRequirement) {
					//TODO - 1.21: Should we be validating we simulate that we will be able to extract the stack and how much of it?
					if (FuelMapper.isStackFuel(stack)) {
						long emc = getEmcValue(stack);
						int toRemove = Mth.ceil((double) (minFuel - emcConsumed) / emc);
						int actualRemoved = Math.min(stack.getCount(), toRemove);
						if (actualRemoved > 0) {
							map.put(i, actualRemoved);
							emcConsumed += emc * actualRemoved;
							metRequirement = emcConsumed >= minFuel;
						}
					}
				}
			}
			if (metRequirement) {
				for (Int2IntMap.Entry entry : map.int2IntEntrySet()) {
					//TODO - 1.21: Should we be validating we were able to actually extract the items?
					inv.extractItem(entry.getIntKey(), entry.getIntValue(), false);
				}
				//TODO - 1.21: Does this update offhand if we are in a block's gui?
				player.containerMenu.broadcastChanges();
				return emcConsumed;
			}
		}
		return -1;
	}

	private static long tryExtract(@NotNull ItemStack stack, long minFuel) {
		if (!stack.isEmpty()) {
			IItemEmcHolder emcHolder = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
			if (emcHolder != null) {
				long simulatedExtraction = emcHolder.extractEmc(stack, minFuel, EmcAction.SIMULATE);
				if (simulatedExtraction >= minFuel) {
					return emcHolder.extractEmc(stack, simulatedExtraction, EmcAction.EXECUTE);
				}
			}
		}
		return 0;
	}

	public static boolean doesItemHaveEmc(ItemInfo info) {
		return getEmcValue(info) > 0;
	}

	public static boolean doesItemHaveEmc(ItemStack stack) {
		return getEmcValue(stack) > 0;
	}

	public static boolean doesItemHaveEmc(Holder<Item> item) {
		return getEmcValue(item) > 0;
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcValue(Holder<Item> item) {
		return item.isBound() ? getEmcValue(ItemInfo.fromItem(item)) : 0;
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcValue(ItemLike item) {
		return item == null ? 0 : getEmcValue(item.asItem().builtInRegistryHolder());
	}

	/**
	 * Does not consider stack size
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcValue(ItemStack stack) {
		return stack.isEmpty() ? 0 : getEmcValue(ItemInfo.fromStack(stack));
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcValue(ItemInfo info) {
		return DataComponentManager.getEmcValue(info);
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcSellValue(ItemStack stack) {
		return stack.isEmpty() ? 0 : getEmcSellValue(ItemInfo.fromStack(stack));
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcSellValue(ItemInfo info) {
		return getEmcSellValue(getEmcValue(info));
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEmcSellValue(@Range(from = 0, to = Long.MAX_VALUE) long originalValue) {
		if (originalValue == 0) {
			return 0;
		}
		long emc = Mth.lfloor(originalValue * ProjectEConfig.server.difficulty.covalenceLoss.get());
		if (emc < 1) {
			if (ProjectEConfig.server.difficulty.covalenceLossRounding.get()) {
				emc = 1;
			} else {
				emc = 0;
			}
		}
		return emc;
	}

	public static Component getEmcTextComponent(long emc, int stackSize) {
		if (ProjectEConfig.server.difficulty.covalenceLoss.get() == 1.0) {
			ILangEntry prefix;
			String value;
			if (stackSize > 1) {
				prefix = PELang.EMC_STACK_TOOLTIP;
				value = Constants.EMC_FORMATTER.format(BigInteger.valueOf(emc).multiply(BigInteger.valueOf(stackSize)));
			} else {
				prefix = PELang.EMC_TOOLTIP;
				value = Constants.EMC_FORMATTER.format(emc);
			}
			return prefix.translateColored(ChatFormatting.YELLOW, ChatFormatting.WHITE, value);
		}
		//Sell enabled
		long emcSellValue = getEmcSellValue(emc);
		ILangEntry prefix;
		String value;
		String sell;
		if (stackSize > 1) {
			prefix = PELang.EMC_STACK_TOOLTIP_WITH_SELL;
			BigInteger bigIntStack = BigInteger.valueOf(stackSize);
			value = Constants.EMC_FORMATTER.format(BigInteger.valueOf(emc).multiply(bigIntStack));
			sell = Constants.EMC_FORMATTER.format(BigInteger.valueOf(emcSellValue).multiply(bigIntStack));
		} else {
			prefix = PELang.EMC_TOOLTIP_WITH_SELL;
			value = Constants.EMC_FORMATTER.format(emc);
			sell = Constants.EMC_FORMATTER.format(emcSellValue);
		}
		return prefix.translateColored(ChatFormatting.YELLOW, ChatFormatting.WHITE, value, ChatFormatting.BLUE, sell);
	}

	@Range(from = 0, to = Long.MAX_VALUE)
	public static long getEMCPerDurability(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		} else if (stack.isDamageableItem()) {
			ItemStack stackCopy = stack.copy();
			stackCopy.setDamageValue(0);
			long emc = (long) Math.ceil(getEmcValue(stackCopy) / (double) stack.getMaxDamage());
			return Math.max(emc, 1);
		}
		return 1;
	}

	/**
	 * Adds the given amount to the amount of unprocessed EMC the stack has. The amount returned should be used for figuring out how much EMC actually gets removed. While
	 * the remaining fractional EMC will be stored in UnprocessedEMC.
	 *
	 * @param stack  The stack to set the UnprocessedEMC tag to.
	 * @param amount The partial amount of EMC to add with the current UnprocessedEMC
	 *
	 * @return The amount of non fractional EMC no longer being stored in UnprocessedEMC.
	 */
	public static long removeFractionalEMC(ItemStack stack, double amount) {
		double unprocessedEMC = stack.getOrDefault(PEDataComponentTypes.UNPROCESSED_EMC, 0.0);
		unprocessedEMC += amount;
		long toRemove = (long) unprocessedEMC;
		unprocessedEMC -= toRemove;
		stack.set(PEDataComponentTypes.UNPROCESSED_EMC, unprocessedEMC);
		return toRemove;
	}
}