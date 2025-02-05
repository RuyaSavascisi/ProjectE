package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.items.HyperkineticLens.ExplosiveLensCharge;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class EntityLensProjectile extends NoGravityThrowableProjectile {

	private ExplosiveLensCharge charge;

	public EntityLensProjectile(EntityType<EntityLensProjectile> type, Level level) {
		super(type, level);
	}

	public EntityLensProjectile(Player entity, ExplosiveLensCharge charge) {
		super(PEEntityTypes.LENS_PROJECTILE.get(), entity, entity.level());
		this.charge = charge;
	}

	@Override
	protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide && isAlive() && isInWater()) {
			playSound(SoundEvents.GENERIC_BURN, 0.7F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
			((ServerLevel) level()).sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 2, 0, 0, 0, 0);
			discard();
		}
	}

	@Override
	protected void onHit(@NotNull HitResult result) {
		if (!level().isClientSide) {
			WorldHelper.createNovaExplosion(level(), getOwner(), getX(), getY(), getZ(), charge.radius());
		}
		//TODO - 1.21: Should we be calling super instead for handling the game events etc
		gameEvent(GameEvent.PROJECTILE_LAND, getOwner());
		discard();
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putInt("charge", charge.ordinal());
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		charge = ExplosiveLensCharge.BY_ID.apply(nbt.getInt("charge"));
	}

	@Override
	public boolean ignoreExplosion(@NotNull Explosion explosion) {
		return true;
	}
}