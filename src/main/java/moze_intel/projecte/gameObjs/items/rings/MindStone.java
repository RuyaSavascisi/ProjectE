package moze_intel.projecte.gameObjs.items.rings;

import com.google.common.collect.Lists;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MindStone extends PEToggleItem implements IPedestalItem {

	private static final int TRANSFER_RATE = 50;

	public MindStone(Properties props) {
		super(props.component(PEDataComponentTypes.STORED_EXP, 0));
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isHeld) {
		super.inventoryTick(stack, level, entity, slot, isHeld);
		if (!level.isClientSide && hotBarOrOffHand(slot) && entity instanceof Player player) {
			if (stack.getOrDefault(PEDataComponentTypes.ACTIVE, false) && getXP(player) > 0) {
				int toAdd = Math.min(getXP(player), TRANSFER_RATE);
				addStoredXP(stack, toAdd);
				removeXP(player, TRANSFER_RATE);
			}
		}
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && !stack.getOrDefault(PEDataComponentTypes.ACTIVE, false) && getStoredXP(stack) != 0) {
			int toAdd = removeStoredXP(stack, TRANSFER_RATE);
			if (toAdd > 0) {
				addXP(player, toAdd);
			}
		}
		return InteractionResultHolder.success(stack);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		int storedXp = stack.getOrDefault(PEDataComponentTypes.STORED_EXP, 0);
		tooltip.add(PELang.TOOLTIP_STORED_XP.translateColored(ChatFormatting.DARK_GREEN, ChatFormatting.GREEN, String.format("%,d", storedXp)));
	}


	private void removeXP(Player player, int amount) {
		int totalExperience = getXP(player) - amount;
		if (totalExperience < 0) {
			player.totalExperience = 0;
			player.experienceLevel = 0;
			player.experienceProgress = 0;
		} else {
			player.totalExperience = totalExperience;
			player.experienceLevel = getLvlForXP(totalExperience);
			player.experienceProgress = (float) (totalExperience - getXPForLvl(player.experienceLevel)) / (float) player.getXpNeededForNextLevel();
		}
	}

	private void addXP(Player player, int amount) {
		int experiencetotal = getXP(player) + amount;
		player.totalExperience = experiencetotal;
		player.experienceLevel = getLvlForXP(experiencetotal);
		player.experienceProgress = (float) (experiencetotal - getXPForLvl(player.experienceLevel)) / (float) player.getXpNeededForNextLevel();
	}

	private int getXP(Player player) {
		return (int) (getXPForLvl(player.experienceLevel) + player.experienceProgress * player.getXpNeededForNextLevel());
	}

	// Math referenced from the MC wiki
	private int getXPForLvl(int level) {
		if (level < 0) {
			return Integer.MAX_VALUE;
		}

		if (level <= 16) {
			return level * level + 6 * level;
		}

		if (level <= 31) {
			return (int) (level * level * 2.5D - 40.5D * level + 360.0D);
		}

		return (int) (level * level * 4.5D - 162.5D * level + 2220.0D);
	}

	private int getLvlForXP(int totalXP) {
		int result = 0;

		while (getXPForLvl(result) <= totalXP) {
			result++;
		}

		return --result;
	}

	private int getStoredXP(ItemStack stack) {
		return stack.getOrDefault(PEDataComponentTypes.STORED_EXP, 0);
	}

	private void setStoredXP(ItemStack stack, int XP) {
		stack.set(PEDataComponentTypes.STORED_EXP, XP);
	}

	private void addStoredXP(ItemStack stack, int XP) {
		int stored = getStoredXP(stack);
		if (stored > Integer.MAX_VALUE - XP) {
			setStoredXP(stack, Integer.MAX_VALUE);
		} else {
			setStoredXP(stack, stored + XP);
		}
	}

	private int removeStoredXP(ItemStack stack, int XP) {
		int currentXP = getStoredXP(stack);
		int result;
		int returnResult;

		if (currentXP < XP) {
			result = 0;
			returnResult = currentXP;
		} else {
			result = currentXP - XP;
			returnResult = XP;
		}

		setStoredXP(stack, result);
		return returnResult;
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos,
			@NotNull PEDESTAL pedestal) {
		boolean sucked = false;
		Vec3 target = pos.getCenter();
		for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, pedestal.getEffectBounds())) {
			WorldHelper.gravitateEntityTowards(orb, target);
			if (!level.isClientSide && orb.distanceToSqr(target) < 1.21) {
				suckXP(orb, stack);
				sucked = true;
			}
		}
		return sucked;
	}

	private void suckXP(ExperienceOrb orb, ItemStack mindStone) {
		long l = getStoredXP(mindStone);
		if (l + orb.value > Integer.MAX_VALUE) {
			orb.value = (int) (l + orb.value - Integer.MAX_VALUE);
			setStoredXP(mindStone, Integer.MAX_VALUE);
		} else {
			addStoredXP(mindStone, orb.value);
			orb.discard();
		}
	}

	@NotNull
	@Override
	public List<Component> getPedestalDescription(float tickRate) {
		return Lists.newArrayList(PELang.PEDESTAL_MIND_STONE.translateColored(ChatFormatting.BLUE));
	}
}