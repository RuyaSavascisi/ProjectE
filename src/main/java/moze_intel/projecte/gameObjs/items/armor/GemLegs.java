package moze_intel.projecte.gameObjs.items.armor;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.util.List;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

public class GemLegs extends GemArmorBase {

	private static final Vec3 DOWNWARD_MOVEMENT = new Vec3(0, -0.32F, 0);

	public GemLegs(Properties props) {
		super(ArmorItem.Type.LEGGINGS, props);
		NeoForge.EVENT_BUS.addListener(this::onJump);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(PELang.GEM_LORE_LEGS.translate());
	}

	private final Int2LongMap lastJumpTracker = new Int2LongOpenHashMap();

	private void onJump(LivingEvent.LivingJumpEvent evt) {
		if (evt.getEntity() instanceof Player player && player.level().isClientSide) {
			lastJumpTracker.put(player.getId(), player.level().getGameTime());
		}
	}

	private boolean jumpedRecently(Player player) {
		return lastJumpTracker.containsKey(player.getId()) && player.level().getGameTime() - lastJumpTracker.get(player.getId()) < 5;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean isHeld) {
		super.inventoryTick(stack, level, entity, slot, isHeld);
		if (isArmorSlot(slot) && entity instanceof Player player) {
			if (level.isClientSide) {
				if (player.isSecondaryUseActive() && !player.onGround() && player.getDeltaMovement().y() > -8 && !jumpedRecently(player)) {
					player.addDeltaMovement(DOWNWARD_MOVEMENT);
				}
			}
			if (player.isSecondaryUseActive()) {
				WorldHelper.repelEntitiesSWRG(level, player.getBoundingBox().inflate(3.5), player);
				if (!level.isClientSide && player.getDeltaMovement().y() < -0.08) {
					for (Entity e : player.level().getEntities(player,
							player.getBoundingBox().move(player.getDeltaMovement()).inflate(2.0D),
							ent -> ent.isAlive() && ent.isPickable() && ent instanceof LivingEntity
					)) {
						e.hurt(level.damageSources().playerAttack(player), (float) -player.getDeltaMovement().y() * 6F);
					}
				}
			}
		}
	}
}