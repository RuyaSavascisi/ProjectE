package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.function.IntBinaryOperator;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.math3.fraction.BigFraction;

public final class Constants {

	public static final NumberFormat EMC_FORMATTER = getFormatter();

	private static NumberFormat getFormatter() {
		NumberFormat format = NumberFormat.getInstance();
		//Only ever use a single decimal point for our formatter,
		// because the majority of the time we are a whole number
		// except for when we are abbreviating
		format.setMaximumFractionDigits(1);
		return format;
	}

	public static final IntBinaryOperator INT_SUM = Integer::sum;

	public static final BigInteger MAX_EXACT_TRANSMUTATION_DISPLAY = BigInteger.valueOf(1_000_000_000_000L);
	public static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);
	public static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
	public static final BigInteger FREE_BIG_INT_VALUE = BigInteger.valueOf(ProjectEAPI.FREE_ARITHMETIC_VALUE);
	public static final BigFraction FREE_FRACTION_VALUE = new BigFraction(FREE_BIG_INT_VALUE);

	public static final float[] EXPLOSIVE_LENS_RADIUS = new float[]{4.0F, 8.0F, 12.0F, 16.0F, 16.0F, 16.0F, 16.0F, 16.0F};
	public static final long[] EXPLOSIVE_LENS_COST = new long[]{384, 768, 1536, 2304, 2304, 2304, 2304, 2304};

	public static final int MAX_CONDENSER_PROGRESS = 102;

	public static final int MAX_VEIN_SIZE = 250;
	public static final int TICKS_PER_HALF_SECOND = SharedConstants.TICKS_PER_SECOND / 2;

	/**
	 * @apiNote DO NOT MODIFY THE BACKING ARRAY
	 */
	public static final Direction[] DIRECTIONS = Direction.values();
	/**
	 * @apiNote DO NOT MODIFY THE BACKING ARRAY
	 */
	public static final DyeColor[] COLORS = DyeColor.values();
}