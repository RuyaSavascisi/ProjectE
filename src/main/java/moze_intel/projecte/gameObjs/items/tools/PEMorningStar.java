package moze_intel.projecte.gameObjs.items.tools;

import java.util.List;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.IMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IHasConditionalAttributes;
import moze_intel.projecte.gameObjs.items.IItemMode;
import moze_intel.projecte.gameObjs.items.tools.PEPickaxe.PickaxeMode;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.ToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;

public class PEMorningStar extends PETool implements IItemMode<PickaxeMode>, IHasConditionalAttributes {

	public PEMorningStar(IMatterType matterType, int numCharges, Properties props) {
		super(matterType, PETags.Blocks.MINEABLE_WITH_PE_MORNING_STAR, numCharges, props.attributes(createAttributes(matterType, 16, -3))
				.component(PEDataComponentTypes.PICKAXE_MODE, PickaxeMode.STANDARD)
		);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(getToolTip(stack));
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ItemAbility toolAction) {
		return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(toolAction) ||
			   ToolHelper.DEFAULT_PE_HAMMER_ACTIONS.contains(toolAction) || ToolHelper.DEFAULT_PE_MORNING_STAR_ACTIONS.contains(toolAction);
	}

	@Override
	public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity damaged, @NotNull LivingEntity damager) {
		ToolHelper.attackWithCharge(stack, damaged, damager, 1.0F);
		return true;
	}

	@Override
	public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity living) {
		ToolHelper.digBasedOnMode(stack, level, pos, living, Item::getPlayerPOVHitResult, getMode(stack));
		return true;
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		InteractionHand hand = context.getHand();
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction sideHit = context.getClickedFace();
		ItemStack stack = context.getItemInHand();
		BlockState state = level.getBlockState(pos);
		//Order that it attempts to use the item:
		// Till (Shovel), Vein (or AOE) mine gravel/clay, vein mine ore, AOE dig (if it is sand, dirt, or grass don't do depth)
		return ToolHelper.performActions(ToolHelper.flattenAOE(context, state, 0),
				() -> ToolHelper.dowseCampfire(context, state),
				() -> {
					if (state.is(Tags.Blocks.GRAVELS) || state.is(Blocks.CLAY)) {
						if (ProjectEConfig.server.items.pickaxeAoeVeinMining.get()) {
							return ToolHelper.digAOE(level, player, hand, stack, pos, sideHit, false, 0);
						}
						return ToolHelper.tryVeinMine(player, stack, pos, sideHit);
					}
					return InteractionResult.PASS;
				}, () -> {
					if (ItemHelper.isOre(state) && !ProjectEConfig.server.items.pickaxeAoeVeinMining.get()) {
						return ToolHelper.tryVeinMine(player, stack, pos, sideHit);
					}
					return InteractionResult.PASS;
				}, () -> ToolHelper.digAOE(level, player, hand, stack, pos, sideHit,
						!(state.getBlock() instanceof GrassBlock) && !state.is(BlockTags.SAND) && !state.is(BlockTags.DIRT), 0));
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (ProjectEConfig.server.items.pickaxeAoeVeinMining.get()) {
			return ItemHelper.actionResultFromType(ToolHelper.mineOreVeinsInAOE(player, hand), stack);
		}
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		return ToolHelper.canMatterMine(matterType, state.getBlock()) ? 1_200_000 : super.getDestroySpeed(stack, state) + 48.0F;
	}

	@Override
	public void adjustAttributes(ItemAttributeModifierEvent event) {
		ToolHelper.applyChargeAttributes(event);
	}

	@Override
	public DataComponentType<PickaxeMode> getDataComponentType() {
		return PEDataComponentTypes.PICKAXE_MODE.get();
	}

	@Override
	public PickaxeMode getDefaultMode() {
		return PickaxeMode.STANDARD;
	}
}