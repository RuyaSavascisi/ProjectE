package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.PESounds;
import moze_intel.projecte.api.item.IExtraFunction;
import moze_intel.projecte.api.item.IProjectileShooter;
import moze_intel.projecte.gameObjs.entity.EntityMobRandomizer;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldTransmutations;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Particles;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhilosophersStone extends ItemMode implements IProjectileShooter, IExtraFunction
{
	public PhilosophersStone(Properties props)
	{
		super(props, (byte)4, new String[] {
				"pe.philstone.mode1",
				"pe.philstone.mode2",
				"pe.philstone.mode3"});
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return true;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		return stack.copy();
	}

	public RayTraceResult getHitBlock(EntityPlayer player)
	{
		return rayTrace(player.getEntityWorld(), player, player.isSneaking());
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemUseContext ctx)
	{
		BlockPos pos = ctx.getPos();
		EnumFacing sideHit = ctx.getFace();
		World world = ctx.getWorld();
		EntityPlayer player = ctx.getPlayer();
		ItemStack stack = ctx.getItem();

		if (world.isRemote)
		{
			return EnumActionResult.SUCCESS;
		}

		RayTraceResult rtr = getHitBlock(player);

		if (rtr != null && rtr.getBlockPos() != null && !rtr.getBlockPos().equals(pos))
		{
			pos = rtr.getBlockPos();
			sideHit = rtr.sideHit;
		}

		IBlockState result = WorldTransmutations.getWorldTransmutation(world, pos, player.isSneaking());

		if (result != null)
		{
			int mode = this.getMode(stack);
			int charge = this.getCharge(stack);

			for (BlockPos currentPos : getAffectedPositions(world, pos, player, sideHit, mode, charge))
			{
				PlayerHelper.checkedReplaceBlock(((EntityPlayerMP) player), currentPos, result);
				if (world.rand.nextInt(8) == 0)
				{
					((WorldServer) world).spawnParticle(Particles.LARGE_SMOKE, currentPos.getX(), currentPos.getY() + 1, currentPos.getZ(), 2, 0, 0, 0, 0);
				}
			}

			world.playSound(null, player.posX, player.posY, player.posZ, PESounds.TRANSMUTE, SoundCategory.PLAYERS, 1, 1);

			// todo 1.13 PlayerHelper.swingItem(player, hand);
		}
		
		return EnumActionResult.SUCCESS;
	}

	@Override
	public boolean shootProjectile(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, EnumHand hand)
	{
		World world = player.getEntityWorld();
		world.playSound(null, player.posX, player.posY, player.posZ, PESounds.TRANSMUTE, SoundCategory.PLAYERS, 1, 1);
		EntityMobRandomizer ent = new EntityMobRandomizer(player, world);
		ent.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.5F, 1);
		world.spawnEntity(ent);
		return true;
	}
	
	@Override
	public boolean doExtraFunction(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, EnumHand hand)
	{
		if (!player.getEntityWorld().isRemote)
		{
			// todo 1.13 player.openGui(PECore.instance, Constants.PHILOS_STONE_GUI, player.getEntityWorld(), hand == EnumHand.MAIN_HAND ? 0 : 1, -1, -1);
		}

		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flags)
	{
		list.add(new TextComponentTranslation("pe.philstone.tooltip1", ClientKeyHelper.getKeyName(PEKeybind.EXTRA_FUNCTION)));
	}

	public static Set<BlockPos> getAffectedPositions(World world, BlockPos pos, EntityPlayer player, EnumFacing sideHit, int mode, int charge)
	{
		Set<BlockPos> ret = new HashSet<>();
		IBlockState targeted = world.getBlockState(pos);
		Iterable<BlockPos> iterable = null;

		switch (mode)
		{
			case 0: // Cube
				iterable = BlockPos.getAllInBox(pos.add(-charge, -charge, -charge), pos.add(charge, charge, charge));
				break;
			case 1: // Panel
				if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN)
				{
					iterable = BlockPos.getAllInBox(pos.add(-charge, 0, -charge), pos.add(charge, 0, charge));
				}
				else if (sideHit == EnumFacing.EAST || sideHit == EnumFacing.WEST)
				{
					iterable = BlockPos.getAllInBox(pos.add(0, -charge, -charge), pos.add(0, charge, charge));
				}
				else if (sideHit == EnumFacing.SOUTH || sideHit == EnumFacing.NORTH)
				{
					iterable = BlockPos.getAllInBox(pos.add(-charge, -charge, 0), pos.add(charge, charge, 0));
				}
				break;
			case 2: // Line
				EnumFacing playerFacing = player.getHorizontalFacing();

				if (playerFacing.getAxis() == EnumFacing.Axis.Z)
				{
					iterable = BlockPos.getAllInBox(pos.add(0, 0, -charge), pos.add(0, 0, charge));
				}
				else if (playerFacing.getAxis() == EnumFacing.Axis.X)
				{
					iterable = BlockPos.getAllInBox(pos.add(-charge, 0, 0), pos.add(charge, 0, 0));
				}
				break;
		}

		if (iterable != null) {
			for (BlockPos currentPos : iterable)
            {
                if (world.getBlockState(currentPos) == targeted)
                {
                    ret.add(currentPos);
                }
            }
		}

		return ret;
	}
}
