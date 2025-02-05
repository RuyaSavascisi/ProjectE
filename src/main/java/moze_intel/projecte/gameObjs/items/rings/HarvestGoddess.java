package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

public class HarvestGoddess extends PEToggleItem implements IPedestalItem {

	public HarvestGoddess(Properties props) {
		super(props.component(PEDataComponentTypes.STORED_EMC, 0L)
				.component(PEDataComponentTypes.UNPROCESSED_EMC, 0.0)
		);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isHeld) {
		super.inventoryTick(stack, level, entity, slot, isHeld);
		if (level.isClientSide || !hotBarOrOffHand(slot) || !(entity instanceof Player player)) {
			return;
		}
		if (stack.getOrDefault(PEDataComponentTypes.ACTIVE, false)) {
			if (!hasEmc(player, stack, 64, true)) {
				stack.set(PEDataComponentTypes.ACTIVE, false);
			} else {
				WorldHelper.growNearbyRandomly(true, level, player);
				removeEmc(stack, 0.32F);
			}
		} else {
			WorldHelper.growNearbyRandomly(false, level, player);
		}
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		BlockPos pos = ctx.getClickedPos();
		Direction side = ctx.getClickedFace();
		if (level.isClientSide || player == null || !player.mayUseItemAt(pos, side, ctx.getItemInHand())) {
			return InteractionResult.FAIL;
		}
		if (player.isSecondaryUseActive()) {
			for (int i = 0; i < player.getInventory().items.size(); i++) {
				ItemStack stack = player.getInventory().items.get(i);
				if (!stack.isEmpty() && stack.getCount() >= 4 && stack.is(Items.BONE_MEAL)) {
					if (useBoneMeal(level, pos, side)) {
						player.getInventory().removeItem(i, 4);
						player.inventoryMenu.broadcastChanges();
						return InteractionResult.CONSUME;
					}
					break;
				}
			}
		} else if (plantSeeds(level, player, pos)) {
			return InteractionResult.CONSUME;
		}
		return InteractionResult.FAIL;
	}

	private boolean useBoneMeal(Level level, BlockPos pos, Direction side) {
		if (level instanceof ServerLevel serverLevel) {
			boolean result = false;
			for (BlockPos currentPos : WorldHelper.horizontalPositionsAround(pos, 15)) {
				currentPos = currentPos.immutable();
				BlockState state = serverLevel.getBlockState(currentPos);
				if (state.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(serverLevel, currentPos, state) &&
					growable.isBonemealSuccess(serverLevel, serverLevel.random, currentPos, state)) {
					growable.performBonemeal(serverLevel, serverLevel.random, currentPos, state);
					level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, currentPos, 0);
					result = true;
				} else if (WorldHelper.growWaterPlant(serverLevel, currentPos, state, side)) {
					level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, currentPos, 0);
					result = true;
				}
			}
			return result;
		}
		return false;
	}

	private boolean plantSeeds(Level level, Player player, BlockPos pos) {
		List<StackWithSlot> seeds = getAllSeeds(player.getInventory().items);
		if (seeds.isEmpty()) {
			return false;
		}
		boolean result = false;
		for (BlockPos currentPos : WorldHelper.horizontalPositionsAround(pos, 8)) {
			BlockState state = level.getBlockState(currentPos);
			if (state.isAir()) {
				continue;
			}
			//Ensure we are immutable so that changing blocks doesn't act weird
			currentPos = currentPos.immutable();
			BlockPos plantPos = currentPos.above();
			for (Iterator<StackWithSlot> iterator = seeds.iterator(); iterator.hasNext(); ) {
				StackWithSlot s = iterator.next();
				//TODO - 1.21: Figure this out
				ItemStack stack = player.getInventory().getItem(s.slot);
				if (stack.isEmpty()) {
					iterator.remove();
					continue;
				}
				boolean planted = false;
				if (stack.getItem() instanceof SpecialPlantable plantable && plantable.canPlacePlantAtPosition(stack, level, plantPos, Direction.DOWN)) {
					plantable.spawnPlantAtPosition(stack, level, plantPos, Direction.DOWN);
					planted = true;
				} else if (!stack.isEmpty() && stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS) && stack.getItem() instanceof BlockItem && level.isEmptyBlock(plantPos)) {
					//TODO - 1.21: Which way should we get the state for placement
					BlockPlaceContext context = null;/*new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND,
							new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false)));*/
					BlockState plantState = ItemHelper.stackToState(stack, context);
					if (plantState == null) {
						continue;
					}
					TriState canSustain = state.canSustainPlant(level, currentPos, Direction.UP, plantState);
					//TODO - 1.21: Support the case when canSustain is default
					if (canSustain.isTrue()) {
						level.setBlockAndUpdate(plantPos, plantState);
						level.gameEvent(GameEvent.BLOCK_PLACE, plantPos, GameEvent.Context.of(player, plantState));
						planted = true;
					}
				}
				if (planted) {
					player.getInventory().removeItem(s.slot, 1);
					player.inventoryMenu.broadcastChanges();
					if (--s.count == 0) {
						iterator.remove();
						if (seeds.isEmpty()) {
							//If we are out of seeds, hard exit the method
							return true;
						}
					}
					if (!result) {
						result = true;
					}
					//Once we set a seed in that position, break out of trying to place other seeds in that position
					break;
				}
			}
		}
		return result;
	}

	private List<StackWithSlot> getAllSeeds(NonNullList<ItemStack> inv) {
		List<StackWithSlot> result = new ArrayList<>();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.get(i);
			if (!stack.isEmpty()) {
				Item item = stack.getItem();
				if (item instanceof SpecialPlantable) {
					result.add(new StackWithSlot(stack, i));
				} else if (item instanceof BlockItem && stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
					//TODO - 1.21: Re-evaluate how we want to handle this check. Do we want a wider tag than just villager plantable seeds?
					result.add(new StackWithSlot(stack, i));
				}
			}
		}
		return result;
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos,
			@NotNull PEDESTAL pedestal) {
		if (!level.isClientSide && ProjectEConfig.server.cooldown.pedestal.harvest.get() != -1) {
			if (pedestal.getActivityCooldown() == 0) {
				WorldHelper.growNearbyRandomly(true, level, pedestal.getEffectBounds(), null);
				pedestal.setActivityCooldown(level, pos, ProjectEConfig.server.cooldown.pedestal.harvest.get());
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
		if (ProjectEConfig.server.cooldown.pedestal.harvest.get() != -1) {
			list.add(PELang.PEDESTAL_HARVEST_GODDESS_1.translateColored(ChatFormatting.BLUE));
			list.add(PELang.PEDESTAL_HARVEST_GODDESS_2.translateColored(ChatFormatting.BLUE));
			list.add(PELang.PEDESTAL_HARVEST_GODDESS_3.translateColored(ChatFormatting.BLUE, MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.harvest.get(), tickRate)));
		}
		return list;
	}

	private static class StackWithSlot {

		public final int slot;
		public int count;

		public StackWithSlot(ItemStack stack, int slot) {
			this.slot = slot;
			this.count = stack.getCount();
		}
	}
}