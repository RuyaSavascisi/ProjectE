package moze_intel.projecte.gameObjs.entity;

import net.minecraft.SharedConstants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class NoGravityThrowableProjectile extends ThrowableProjectile {

	protected NoGravityThrowableProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	protected NoGravityThrowableProjectile(EntityType<? extends ThrowableProjectile> type, LivingEntity shooter, Level level) {
		super(type, shooter, level);
		setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide) {
			if (tickCount > (20 * SharedConstants.TICKS_PER_SECOND) || getDeltaMovement().equals(Vec3.ZERO) || !level().isLoaded(blockPosition())) {
				discard();
			}
		}
	}
}