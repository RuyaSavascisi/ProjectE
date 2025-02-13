package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.util.List;
import java.util.function.BiPredicate;
import moze_intel.projecte.PECore;
import moze_intel.projecte.integration.IntegrationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityMultiPlaceEvent;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Helper class for player-related methods. Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class PlayerHelper {

	public final static ObjectiveCriteria SCOREBOARD_EMC = new ReadOnlyScoreCriteria(PECore.MODID + ":emc_score");

	/**
	 * Tries placing a block and fires an event for it.
	 *
	 * @return Whether the block was successfully placed
	 */
	public static boolean checkedPlaceBlock(Player player, BlockPos pos, BlockState state) {
		return hasEditPermission(player, pos) && partiallyCheckedPlaceBlock(player, pos, state);
	}

	private static boolean partiallyCheckedPlaceBlock(Player player, BlockPos pos, BlockState state) {
		Level level = player.level();
		level.captureBlockSnapshots = true;
		level.setBlockAndUpdate(pos, state);
		level.captureBlockSnapshots = false;

		@SuppressWarnings("unchecked")
		List<BlockSnapshot> blockSnapshots = (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
		level.capturedBlockSnapshots.clear();

		boolean eventResult = false;
		if (blockSnapshots.size() > 1) {
			eventResult = NeoForge.EVENT_BUS.post(new EntityMultiPlaceEvent(blockSnapshots, Blocks.AIR.defaultBlockState(), player)).isCanceled();
		} else if (blockSnapshots.size() == 1) {
			eventResult = NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(blockSnapshots.getFirst(), Blocks.AIR.defaultBlockState(), player)).isCanceled();
		}

		if (eventResult) {
			level.restoringBlockSnapshots = true;
			for (BlockSnapshot snapshot : blockSnapshots.reversed()) {
				snapshot.restore(snapshot.getFlags() | Block.UPDATE_CLIENTS);
			}
			level.restoringBlockSnapshots = false;
		} else {
			//Place all the blocks into the world and sync them to the client
			for (BlockSnapshot snap : blockSnapshots) {
				int updateFlag = snap.getFlags();
				BlockState oldBlock = snap.getState();
				BlockState newBlock = level.getBlockState(snap.getPos());
				newBlock.onPlace(level, snap.getPos(), oldBlock, false);
				level.markAndNotifyBlock(snap.getPos(), level.getChunkAt(snap.getPos()), oldBlock, newBlock, updateFlag, Block.UPDATE_LIMIT);
			}
		}
		level.capturedBlockSnapshots.clear();
		return !eventResult;
	}

	public static boolean checkedReplaceBlock(ServerPlayer player, BlockPos pos, BlockState state) {
		return hasBreakPermission(player, pos) && partiallyCheckedPlaceBlock(player, pos, state);
	}

	public static ItemStack findFirstItem(Player player, Holder<Item> consumeFrom) {
		for (ItemStack s : player.getInventory().items) {
			if (!s.isEmpty() && s.is(consumeFrom)) {
				return s;
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean checkHotbarCurios(Player player, BiPredicate<Player, ItemStack> checker) {
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && checker.test(player, stack)) {
				return true;
			}
		}
		ItemStack offhand = player.getOffhandItem();
		if (!offhand.isEmpty() && checker.test(player, offhand)) {
			return true;
		}
		IItemHandler curios = player.getCapability(IntegrationHelper.CURIO_ITEM_HANDLER);
		if (curios != null) {
			for (int i = 0, slots = curios.getSlots(); i < slots; i++) {
				ItemStack stack = curios.getStackInSlot(i);
				if (!stack.isEmpty() && checker.test(player, stack)) {
					return true;
				}
			}
		}
		return false;
	}

	public static BlockHitResult getBlockLookingAt(Player player, double maxDistance) {
		return (BlockHitResult) player.pick(maxDistance, 1.0F, false);
	}

	/**
	 * Returns a vec representing where the player is looking, capped at maxDistance away.
	 */
	public static Vec3 getLookTarget(Player player, double maxDistance) {
		//TODO - 1.21: For both this and getBlockLookingAt, make sure we properly make use of the player interaction range attributes
		Vec3 lookAngle = player.getLookAngle();
		return player.getEyePosition().add(lookAngle.x * maxDistance, lookAngle.y * maxDistance, lookAngle.z * maxDistance);
	}

	public static boolean hasBreakPermission(ServerPlayer player, BlockPos pos) {
		return hasEditPermission(player, pos) && checkBreakPermission(player, pos);
	}

	static boolean checkBreakPermission(ServerPlayer player, BlockPos pos) {
		Level level = player.level();
		return !CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, level.getBlockState(pos)).isCanceled();
	}

	public static boolean hasEditPermission(Player player, BlockPos pos) {
		if (!player.mayInteract(player.level(), pos)) {
			return false;
		}
		for (Direction e : Constants.DIRECTIONS) {
			if (!player.mayUseItemAt(pos, e, ItemStack.EMPTY)) {
				return false;
			}
		}
		return true;
	}

	public static void resetCooldown(Player player) {
		player.resetAttackStrengthTicker();
		PECore.packetHandler().resetCooldown((ServerPlayer) player);
	}

	public static void swingItem(Player player, InteractionHand hand) {
		if (player.level() instanceof ServerLevel level) {
			level.getChunkSource().broadcastAndSend(player, new ClientboundAnimatePacket(player, hand == InteractionHand.MAIN_HAND ? 0 : 3));
		}
	}

	public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, BigInteger value) {
		updateScore(player, objective, MathUtils.clampToInt(value));
	}

	public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, int value) {
		player.getScoreboard().forAllObjectives(objective, player, score -> score.set(value));
	}
}