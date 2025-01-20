package moze_intel.projecte.api.capabilities;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.math.BigInteger;
import java.util.Set;
import java.util.function.IntFunction;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * This interface defines the contract for some object that exposes transmutation knowledge through the Capability system.
 * <p>
 * Acquire an instance of this using {@link net.minecraft.world.entity.Entity#getCapability(EntityCapability)}.
 */
public interface IKnowledgeProvider {

	/**
	 * @return Whether the player has the "tome" flag set, meaning all knowledge checks automatically return true
	 */
	boolean hasFullKnowledge();

	/**
	 * @param fullKnowledge Whether the player has the "tome" flag set, meaning all knowledge checks automatically return true
	 */
	void setFullKnowledge(boolean fullKnowledge);

	/**
	 * Clears all knowledge. Additionally, clears the "tome" flag.
	 */
	void clearKnowledge();

	/**
	 * @param stack The stack to query
	 *
	 * @return Whether the player has transmutation knowledge for this stack
	 *
	 * @implNote This method defaults to making sure the stack is not empty and then wrapping the stack into an {@link ItemInfo} and calling {@link
	 * #hasKnowledge(ItemInfo)}
	 */
	default boolean hasKnowledge(@NotNull ItemStack stack) {
		return !stack.isEmpty() && hasKnowledge(ItemInfo.fromStack(stack));
	}

	/**
	 * @param info The {@link ItemInfo} to query
	 *
	 * @return Whether the player has transmutation knowledge for this {@link ItemInfo}
	 */
	boolean hasKnowledge(@NotNull ItemInfo info);

	/**
	 * @param stack The stack to add to knowledge
	 *
	 * @return Whether the operation was successful
	 *
	 * @implNote This method defaults to making sure the stack is not empty and then wrapping the stack into an {@link ItemInfo} and calling {@link
	 * #addKnowledge(ItemInfo)}
	 */
	default boolean addKnowledge(@NotNull ItemStack stack) {
		return !stack.isEmpty() && addKnowledge(ItemInfo.fromStack(stack));
	}

	/**
	 * @param info The {@link ItemInfo} to add to knowledge
	 *
	 * @return Whether the operation was successful
	 */
	boolean addKnowledge(@NotNull ItemInfo info);

	/**
	 * @param stack The stack to remove from knowledge
	 *
	 * @return Whether the operation was successful
	 *
	 * @implNote This method defaults to making sure the stack is not empty and then wrapping the stack into an {@link ItemInfo} and calling {@link
	 * #removeKnowledge(ItemInfo)}
	 */
	default boolean removeKnowledge(@NotNull ItemStack stack) {
		return !stack.isEmpty() && removeKnowledge(ItemInfo.fromStack(stack));
	}

	/**
	 * @param info The {@link ItemInfo} to remove from knowledge
	 *
	 * @return Whether the operation was successful
	 */
	boolean removeKnowledge(@NotNull ItemInfo info);

	/**
	 * @return An unmodifiable but live view of the knowledge list.
	 */
	@NotNull
	Set<ItemInfo> getKnowledge();

	/**
	 * @return The player's input and lock slots
	 */
	@NotNull
	IItemHandler getInputAndLocks();

	/**
	 * @return The emc in this player's transmutation tablet network
	 */
	BigInteger getEmc();

	/**
	 * @param emc The emc to set in this player's transmutation tablet network
	 */
	void setEmc(BigInteger emc);

	/**
	 * Syncs this provider to the given player.
	 *
	 * @param player The player to sync to.
	 */
	void sync(@NotNull ServerPlayer player);

	/**
	 * Syncs the emc stored in this provider to the given player.
	 *
	 * @param player The player to sync to.
	 */
	void syncEmc(@NotNull ServerPlayer player);

	/**
	 * Syncs that a specific item's knowledge changed (either learned or unlearned) to the given player.
	 *
	 * @param player  The player to sync to.
	 * @param change  The item that changed. (Should be the persistent variant)
	 * @param learned True if learned, false if unlearned.
	 */
	void syncKnowledgeChange(@NotNull ServerPlayer player, ItemInfo change, boolean learned);

	/**
	 * Syncs the inputs and locks stored in this provider to the given player.
	 *
	 * @param player        The player to sync to.
	 * @param slotsChanged  The indices of the slots that need to be synced (may be empty, in which case nothing should happen).
	 * @param updateTargets How the targets should be updated on the client.
	 */
	void syncInputAndLocks(@NotNull ServerPlayer player, IntList slotsChanged, TargetUpdateType updateTargets);

	/**
	 * @param changes Slot index to stack for the changes that occurred.
	 *
	 * @apiNote Should only really be used on the client for purposes of receiving/handling {@link #syncInputAndLocks(ServerPlayer, IntList, TargetUpdateType)}
	 */
	void receiveInputsAndLocks(Int2ObjectMap<ItemStack> changes);

	enum TargetUpdateType {
		/**
		 * Don't update targets.
		 */
		NONE,
		/**
		 * Only update if "needed", the emc value is below the highest item.
		 */
		IF_NEEDED,
		/**
		 * Update targets.
		 */
		ALL;

		public static final IntFunction<TargetUpdateType> BY_ID = ByIdMap.continuous(TargetUpdateType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, TargetUpdateType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TargetUpdateType::ordinal);
	}
}