package moze_intel.projecte.gameObjs.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Internal interface for PlayerChecks.
 */
public interface IFireProtector {

	/**
	 * @return If this stack currently should protect the bearer from fire
	 */
	default boolean canProtectAgainstFire(ItemStack stack, Player player) {
		return true;
	}
}