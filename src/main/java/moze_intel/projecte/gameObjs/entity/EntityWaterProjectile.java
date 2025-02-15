package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class EntityWaterProjectile extends NoGravityThrowableProjectile {

	public EntityWaterProjectile(EntityType<EntityWaterProjectile> type, Level level) {
		super(type, level);
	}

	public EntityWaterProjectile(Player entity, Level level) {
		super(PEEntityTypes.WATER_PROJECTILE.get(), entity, level);
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
					BlockState state = level.getBlockState(pos);
					FluidState fluidState = state.getFluidState();
					if (fluidState.is(FluidTags.LAVA)) {
						pos = pos.immutable();
						if (state.getBlock() instanceof LiquidBlock) {
							//If it is a source block convert it
							Block block = fluidState.isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
							//Like: ForgeEventFactory#fireFluidPlaceBlockEvent except checks if it was cancelled
							BlockEvent.FluidPlaceBlockEvent event = new BlockEvent.FluidPlaceBlockEvent(level, pos, pos, block.defaultBlockState());
							if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
								PlayerHelper.checkedPlaceBlock(player, level, pos, event.getNewState());
							}
						} else {
							//Otherwise if it is lava logged, "void" the lava as we can't place a block in that spot
							WorldHelper.drainFluid(player, level, pos, state, Fluids.LAVA);
						}
						playSound(SoundEvents.GENERIC_BURN, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
					}
				}
			}
			if (isInWater()) {
				discard();
			}
			if (getY() > level.getMaxBuildHeight()) {
				LevelData worldInfo = level.getLevelData();
				worldInfo.setRaining(true);
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
			WorldHelper.placeFluid(player, level(), result.getBlockPos(), result.getDirection(), Fluids.WATER, !ProjectEConfig.server.items.opEvertide.get());
		}
	}

	@Override
	protected void onHitEntity(@NotNull EntityHitResult result) {
		super.onHitEntity(result);
		if (!level().isClientSide) {
			Entity ent = result.getEntity();
			if (ent.isOnFire()) {
				ent.clearFire();
			}
			ent.push(getDeltaMovement().scale(2));
		}
	}

	@Override
	public boolean ignoreExplosion(@NotNull Explosion explosion) {
		return true;
	}
}