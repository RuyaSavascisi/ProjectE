package moze_intel.projecte.gameObjs.items;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.components.GemData;
import moze_intel.projecte.gameObjs.container.EternalDensityContainer;
import moze_intel.projecte.gameObjs.container.inventory.EternalDensityInventory;
import moze_intel.projecte.gameObjs.items.GemEternalDensity.GemMode;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.EntityHandsInvWrapper;
import org.jetbrains.annotations.NotNull;

public class GemEternalDensity extends ItemPE implements IAlchBagItem, IAlchChestItem, IItemMode<GemMode>, ICapabilityAware {

	public GemEternalDensity(Properties props) {
		super(props.component(PEDataComponentTypes.ACTIVE, false)
				.component(PEDataComponentTypes.GEM_MODE, GemMode.IRON)
				.component(PEDataComponentTypes.GEM_DATA, GemData.EMPTY)
				.component(PEDataComponentTypes.STORED_EMC, 0L)
		);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isHeld) {
		super.inventoryTick(stack, level, entity, slot, isHeld);
		if (!level.isClientSide && entity instanceof Player player) {
			condense(stack, new EntityHandsInvWrapper(player));
		}
	}

	/**
	 * @return Whether the inventory was changed
	 */
	private static boolean condense(ItemStack gem, IItemHandler inv) {
		boolean isActive = gem.getOrDefault(PEDataComponentTypes.ACTIVE, false);
		if (!isActive || ItemPE.getEmc(gem) == Constants.BLOCK_ENTITY_MAX_EMC) {
			return false;
		}
		ItemStack target = getTarget(gem);
		long targetEmc = EMCHelper.getEmcValue(target);
		if (targetEmc == 0) {
			//Target doesn't have an EMC value set, just exit early
			return false;
		}
		boolean hasChanged = false;
		GemData gemData = gem.getOrDefault(PEDataComponentTypes.GEM_DATA, GemData.EMPTY);
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			Boolean filtered = null;
			if (!stack.isStackable()) {
				//Only skip unstackable items if they are not explicitly whitelisted
				if (!gemData.isWhitelist()) {
					continue;
				} else if (!gemData.whitelistMatches(stack)) {
					continue;
				}
				filtered = true;
			}

			long emcValue = EMCHelper.getEmcValue(stack);
			if (emcValue == 0 || emcValue >= targetEmc || inv.extractItem(i, stack.getCount() == 1 ? 1 : stack.getCount() / 2, true).isEmpty()) {
				continue;
			}

			if (filtered == null) {
				filtered = gemData.whitelistMatches(stack);
			}
			if (gemData.isWhitelist() == filtered) {
				ItemStack copy = inv.extractItem(i, stack.getCount() == 1 ? 1 : stack.getCount() / 2, false);
				//Note: We add the emc to the stack before adding it to the consumed gem data
				// so that we don't need to worry about addToList mutating the stack
				addEmcToStack(gem, EMCHelper.getEmcValue(copy) * copy.getCount());
				gemData = gemData.addToList(copy);
				gem.set(PEDataComponentTypes.GEM_DATA, gemData);
				hasChanged = true;
				break;
			}
		}

		long value = EMCHelper.getEmcValue(target);
		if (value == 0) {
			return hasChanged;
		}

		while (getEmc(gem) >= value) {
			ItemStack remain = ItemHandlerHelper.insertItemStacked(inv, target.copy(), false);
			if (!remain.isEmpty()) {
				return false;
			}
			removeEmc(gem, value);
			//TODO - 1.21: Re-evaluate what this is meant to be doing
			gemData = gemData.clearConsumed();
			gem.set(PEDataComponentTypes.GEM_DATA, gemData);

			//gem.removeData(PEDataComponentTypes.GEM_CONSUMED);
			hasChanged = true;
		}
		return hasChanged;
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			if (player.isSecondaryUseActive()) {
				if (stack.getOrDefault(PEDataComponentTypes.ACTIVE, false)) {
					GemData oldData = stack.update(PEDataComponentTypes.GEM_DATA, GemData.EMPTY, GemData::clearConsumed);
					if (oldData != null && !oldData.consumed().isEmpty()) {
						WorldHelper.createLootDrop(oldData.consumed(), level, player.position());
						stack.set(PEDataComponentTypes.STORED_EMC, 0L);
					}
					stack.set(PEDataComponentTypes.ACTIVE, false);
				} else {
					stack.set(PEDataComponentTypes.ACTIVE, true);
				}
			} else {
				player.openMenu(new ContainerProvider(hand, stack), buf -> {
					buf.writeEnum(hand);
					buf.writeByte(player.getInventory().selected);
				});
			}
		}
		return InteractionResultHolder.success(stack);
	}

	private static ItemStack getTarget(ItemStack stack) {
		Item item = stack.getItem();
		if (!(item instanceof GemEternalDensity gem)) {
			PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Invalid gem of eternal density: {}", stack);
			return ItemStack.EMPTY;
		}
		return gem.getMode(stack).getTarget();
	}

	@Override
	public DataComponentType<GemMode> getDataComponentType() {
		return PEDataComponentTypes.GEM_MODE.get();
	}

	@Override
	public GemMode getDefaultMode() {
		return GemMode.IRON;
	}

	@Override
	public ILangEntry getModeSwitchEntry() {
		return PELang.DENSITY_MODE_TARGET;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(PELang.TOOLTIP_GEM_DENSITY_1.translate());
		tooltip.add(PELang.TOOLTIP_GEM_DENSITY_2.translate(getMode(stack)));
		tooltip.add(PELang.TOOLTIP_GEM_DENSITY_3.translate(ClientKeyHelper.getKeyName(PEKeybind.MODE)));
		tooltip.add(PELang.TOOLTIP_GEM_DENSITY_4.translate());
		tooltip.add(PELang.TOOLTIP_GEM_DENSITY_5.translate());
	}

	@Override
	public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
		if (!level.isClientSide && stack.getOrDefault(PEDataComponentTypes.ACTIVE, false)) {
			IItemHandler handler = WorldHelper.getCapability(level, ItemHandler.BLOCK, pos, null);
			return handler != null && condense(stack, handler);
		}
		return false;
	}

	@Override
	public boolean updateInAlchBag(@NotNull IItemHandler inv, @NotNull Player player, @NotNull ItemStack stack) {
		return !player.level().isClientSide && condense(stack, inv);
	}

	@Override
	public void attachCapabilities(RegisterCapabilitiesEvent event) {
		IntegrationHelper.registerCuriosCapability(event, this);
	}

	private record ContainerProvider(InteractionHand hand, ItemStack stack) implements MenuProvider {

		@NotNull
		@Override
		public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
			return new EternalDensityContainer(windowId, playerInventory, hand, playerInventory.selected, new EternalDensityInventory(stack));
		}

		@NotNull
		@Override
		public Component getDisplayName() {
			return TextComponentUtil.build(PEItems.GEM_OF_ETERNAL_DENSITY.get());
		}
	}

	public enum GemMode implements IModeEnum<GemMode> {
		IRON(Items.IRON_INGOT),
		GOLD(Items.GOLD_INGOT),
		DIAMOND(Items.DIAMOND),
		DARK_MATTER(PEItems.DARK_MATTER),
		RED_MATTER(PEItems.RED_MATTER);

		public static final Codec<GemMode> CODEC = StringRepresentable.fromEnum(GemMode::values);
		public static final IntFunction<GemMode> BY_ID = ByIdMap.continuous(GemMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, GemMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GemMode::ordinal);

		private final String serializedName;
		private final ItemLike target;

		GemMode(ItemLike target) {
			this.serializedName = name().toLowerCase(Locale.ROOT);
			this.target = target;
		}

		@NotNull
		@Override
		public String getSerializedName() {
			return serializedName;
		}

		@Override
		public String getTranslationKey() {
			return target.asItem().getDescriptionId();
		}

		public ItemStack getTarget() {
			return new ItemStack(target);
		}

		@Override
		public GemMode next(ItemStack stack) {
			return switch (this) {
				case IRON -> GOLD;
				case GOLD -> DIAMOND;
				case DIAMOND -> DARK_MATTER;
				case DARK_MATTER -> RED_MATTER;
				case RED_MATTER -> IRON;
			};
		}
	}
}