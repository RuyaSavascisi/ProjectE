package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class EntityLavaProjectile extends NoGravityThrowableProjectile {

	public EntityLavaProjectile(EntityType<EntityLavaProjectile> type, Level level) {
		super(type, level);
	}

	public EntityLavaProjectile(Player entity, Level level) {
		super(PEEntityTypes.LAVA_PROJECTILE.get(), entity, level);
	}

	@Override
	protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		Level level = level();
		if (!level.isClientSide && isAlive()) {
			if (getOwner() instanceof Player player) {
				for (BlockPos pos : WorldHelper.positionsAround(blockPosition(), 3)) {
					if (level.isLoaded(pos)) {
						BlockState state = level.getBlockState(pos);
						if (state.getFluidState().is(FluidTags.WATER)) {
							pos = pos.immutable();
							if (PlayerHelper.hasEditPermission(player, level, pos)) {
								WorldHelper.drainFluid(player, level, pos, state);
								level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
										2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
							}
						}
					}
				}
			}
			if (getY() > level.getMaxBuildHeight()) {
				LevelData worldInfo = level.getLevelData();
				worldInfo.setRaining(false);
				discard();
			}
		}
	}

	@Override
	protected void onHit(@NotNull HitResult result) {
		super.onHit(result);
		discard();
	}

	@Override
	protected void onHitBlock(@NotNull BlockHitResult result) {
		super.onHitBlock(result);
		if (!level().isClientSide && getOwner() instanceof Player player) {
			ItemStack found = PlayerHelper.findFirstItem(player, PEItems.VOLCANITE_AMULET);
			if (!found.isEmpty() && ItemPE.consumeFuel(player, found, 32, true)) {
				WorldHelper.placeFluid(player, level(), result.getBlockPos(), result.getDirection(), Fluids.LAVA, false);
			}
		}
	}

	@Override
	protected void onHitEntity(@NotNull EntityHitResult result) {
		super.onHitEntity(result);
		if (!level().isClientSide && getOwner() instanceof Player player) {
			ItemStack found = PlayerHelper.findFirstItem(player, PEItems.VOLCANITE_AMULET);
			if (!found.isEmpty() && ItemPE.consumeFuel(player, found, 32, true)) {
				Entity ent = result.getEntity();
				ent.igniteForSeconds(5);
				ent.hurt(level().damageSources().inFire(), 5);
			}
		}
	}

	@Override
	public boolean ignoreExplosion(@NotNull Explosion explosion) {
		return true;
	}
}