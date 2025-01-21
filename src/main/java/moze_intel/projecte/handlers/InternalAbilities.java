package moze_intel.projecte.handlers;

import java.util.function.Predicate;
import moze_intel.projecte.PECore;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

//TODO - 1.21: Validate this works properly given we don't persist it and don't sync it (and it resets on death)
public final class InternalAbilities {

	private static final AttributeModifier FLIGHT = new AttributeModifier(PECore.rl("flight"), 1, Operation.ADD_VALUE);

	private boolean gemArmorReady = false;
	private int projectileCooldown = 0;
	private int gemChestCooldown = 0;

	public void resetProjectileCooldown() {
		projectileCooldown = ProjectEConfig.server.cooldown.player.projectile.get();
	}

	public int getProjectileCooldown() {
		return projectileCooldown;
	}

	public void resetGemCooldown() {
		gemChestCooldown = ProjectEConfig.server.cooldown.player.gemChest.get();
	}

	public int getGemCooldown() {
		return gemChestCooldown;
	}

	public void toggleGemState() {
		gemArmorReady = !gemArmorReady;
	}

	public boolean getGemState() {
		return gemArmorReady;
	}

	// Checks if the server state of player caps mismatches with what ProjectE determines. If so, change it serverside and send a packet to client
	public void tick(Player player) {
		if (projectileCooldown > 0) {
			projectileCooldown--;
		}

		if (gemChestCooldown > 0) {
			gemChestCooldown--;
		}

		//TODO - 1.21: Is it possible to make the swrg attribute apply via the stack in the inventory?
		updateAttribute(player, NeoForgeMod.CREATIVE_FLIGHT, FLIGHT, InternalAbilities::shouldPlayerFly);
	}

	//Note: The attributes that currently use this cannot be converted to just being attributes on the items, as they can be disabled based on the player state
	private void updateAttribute(Player player, Holder<Attribute> attribute, AttributeModifier modifier, Predicate<Player> applyAttribute) {
		AttributeInstance attributeInstance = player.getAttribute(attribute);
		if (attributeInstance != null) {
			boolean hasModifier = attributeInstance.hasModifier(modifier.id());
			if (applyAttribute.test(player)) {
				if (!hasModifier) {
					//Should have it, but doesn't have the modifier yet, add it
					attributeInstance.addTransientModifier(modifier);
				}
			} else if (hasModifier) {
				//Shouldn't have the modifier, remove it
				attributeInstance.removeModifier(modifier.id());
			}
		}
	}

	private static boolean shouldPlayerFly(Player player) {
		return PlayerHelper.checkHotbarCurios(player, (p, stack) -> {
			if (stack.is(PEItems.SWIFTWOLF_RENDING_GALE)) {
				return ItemPE.hasEmc(p, stack, 64, true);
			} /*else if (stack.is(PEItems.ARCANA_RING)) {
				return true;
			}*/
			return false;
		});
	}
}