package moze_intel.projecte.gameObjs.items.tools;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.IMatterType;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.gameObjs.items.IItemMode;
import moze_intel.projecte.gameObjs.items.IModeEnum;
import moze_intel.projecte.gameObjs.items.tools.PEPickaxe.PickaxeMode;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.ToolHelper;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class PEPickaxe extends PickaxeItem implements IItemCharge, IItemMode<PickaxeMode>, IBarHelper {

	private final IMatterType matterType;
	private final int numCharges;

	public PEPickaxe(IMatterType matterType, int numCharges, Properties props) {
		super(matterType, props.attributes(createAttributes(matterType, 4, -2.8F))
				.component(PEDataComponentTypes.PICKAXE_MODE, PickaxeMode.STANDARD)
				.component(PEDataComponentTypes.CHARGE, 0)
		);
		this.matterType = matterType;
		this.numCharges = numCharges;
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	@Override
	public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
		return false;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		return 0;
	}

	@Override
	public boolean isBarVisible(@NotNull ItemStack stack) {
		return true;
	}

	@Override
	public float getWidthForBar(ItemStack stack) {
		return 1 - getChargePercent(stack);
	}

	@Override
	public int getBarWidth(@NotNull ItemStack stack) {
		return getScaledBarWidth(stack);
	}

	@Override
	public int getBarColor(@NotNull ItemStack stack) {
		return getColorForBar(stack);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		if (ToolHelper.canMatterMine(matterType, state.getBlock())) {
			return 1_200_000;
		}
		return ToolHelper.getDestroySpeed(super.getDestroySpeed(stack, state), matterType, getCharge(stack));
	}

	@Override
	public int getNumCharges(@NotNull ItemStack stack) {
		return numCharges;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(getToolTip(stack));
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (ProjectEConfig.server.items.pickaxeAoeVeinMining.get()) {
			//If we are supposed to mine in an AOE then attempt to do so
			return ItemHelper.actionResultFromType(ToolHelper.mineOreVeinsInAOE(player, hand), stack);
		}
		return InteractionResultHolder.pass(stack);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || ProjectEConfig.server.items.pickaxeAoeVeinMining.get()) {
			//If we don't have a player or the config says we should mine in an AOE (this happens when right clicking air as well)
			// Then we just pass so that it can be processed in onItemRightClick
			return InteractionResult.PASS;
		}
		BlockPos pos = context.getClickedPos();
		if (ItemHelper.isOre(context.getLevel().getBlockState(pos))) {
			return ToolHelper.tryVeinMine(player, context.getItemInHand(), pos, context.getClickedFace());
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity living) {
		ToolHelper.digBasedOnMode(stack, level, pos, living, Item::getPlayerPOVHitResult, getMode(stack));
		return true;
	}

	@Override
	public DataComponentType<PickaxeMode> getDataComponentType() {
		return PEDataComponentTypes.PICKAXE_MODE.get();
	}

	@Override
	public PickaxeMode getDefaultMode() {
		return PickaxeMode.STANDARD;
	}

	public enum PickaxeMode implements IModeEnum<PickaxeMode> {
		STANDARD(PELang.MODE_PICK_1),
		TALLSHOT(PELang.MODE_PICK_2),
		WIDESHOT(PELang.MODE_PICK_3),
		LONGSHOT(PELang.MODE_PICK_4);

		public static final Codec<PickaxeMode> CODEC = StringRepresentable.fromEnum(PickaxeMode::values);
		public static final IntFunction<PickaxeMode> BY_ID = ByIdMap.continuous(PickaxeMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, PickaxeMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PickaxeMode::ordinal);

		private final IHasTranslationKey langEntry;
		private final String serializedName;

		PickaxeMode(IHasTranslationKey langEntry) {
			this.serializedName = name().toLowerCase(Locale.ROOT);
			this.langEntry = langEntry;
		}

		@NotNull
		@Override
		public String getSerializedName() {
			return serializedName;
		}

		@Override
		public String getTranslationKey() {
			return langEntry.getTranslationKey();
		}

		@Override
		public PickaxeMode next(ItemStack stack) {
			return switch (this) {
				case STANDARD -> TALLSHOT;
				case TALLSHOT -> WIDESHOT;
				case WIDESHOT -> LONGSHOT;
				case LONGSHOT -> STANDARD;
			};
		}
	}
}