package moze_intel.projecte.emc.arithmetic;

import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.MathUtils;
import org.apache.commons.math3.fraction.BigFraction;

public class FullBigFractionArithmetic implements IValueArithmetic<BigFraction> {

	@Override
	public BigFraction getZero() {
		return BigFraction.ZERO;
	}

	@Override
	public boolean isZero(BigFraction value) {
		return value.getNumerator().signum() == 0;
	}

	@Override
	public boolean isLessThanZero(BigFraction value) {
		return value.getNumerator().signum() == -1;
	}

	@Override
	public boolean isLessThanEqualZero(BigFraction value) {
		return value.getNumerator().signum() != 1;
	}

	@Override
	public boolean isGreaterThanEqualZero(BigFraction value) {
		return value.getNumerator().signum() != -1;
	}

	@Override
	public boolean isGreaterThanZero(BigFraction value) {
		return value.getNumerator().signum() == 1;
	}

	@Override
	public BigFraction add(BigFraction a, BigFraction b) {
		if (isZero(a)) {
			return b;
		} else if (isZero(b)) {
			return a;
		}
		//Note: While this mirrors the checks we just did for if it is zero, we don't just do them as OR so that if b is zero,
		// we don't have to check if "a" is free, as checking for zero is a cheaper check
		if (isFree(a)) {
			return b;
		} else if (isFree(b)) {
			return a;
		}
		return a.add(b);
	}

	@Override
	public BigFraction mul(long a, BigFraction b) {
		if (a == 1 || isZeroOrFree(b)) {
			//If multiplying by 1 then b
			//If b == 0 then 0 (which can just be represented as b)
			//If b is free then result is free (which can just be represented as b)
			return b;
		}
		return b.multiply(a);
	}

	@Override
	public BigFraction div(BigFraction a, long b) {
		if (b == 1 || isZeroOrFree(a)) {
			//If dividing by 1 then "a"
			//If a == 0 then 0 (which can just be represented as a)
			//If "a" is free then result is free (which can just be represented as a)
			return a;
		} else if (b == 0) {//Invalid denominator, we can't divide
			return getZero();
		}
		BigFraction result = a.divide(b);
		//TODO: I believe checking the numerator is not necessary as we will clamp it further down the line
		if (/*MathUtils.isGreaterThanLong(result.getNumerator()) ||*/ MathUtils.isGreaterThanLong(result.getDenominator())) {
			//Overflowed a long as BigFraction can go past Long.MAX_VALUE
			// This means we reached (something > 1) /infinity, which is ~0
			return getZero();
		}
		return result;
	}

	@Override
	public BigFraction getFree() {
		return Constants.FREE_FRACTION_VALUE;
	}

	private boolean isZeroOrFree(BigFraction value) {
		//noinspection NumberEquality
		if (value == getFree()) {
			//Note: We intentionally do identity checking as it is quicker than getting the numerator,
			// and as there is at least sometimes where it is the case
			return true;
		}
		//The following check is quicker than getting the numerator from a big int, and can get rid of a good number of
		// potential values, so we check it first
		int sign = value.getNumerator().signum();
		if (sign == 0) {
			return true;
		} else if (sign == 1) {
			return false;
		}
		return value.getNumerator().equals(Constants.FREE_BIG_INT_VALUE);
	}

	@Override
	public boolean isFree(BigFraction value) {
		//noinspection NumberEquality
		if (value == getFree()) {
			//Note: We intentionally do identity checking as it is quicker than getting the numerator,
			// and as there is at least sometimes where it is the case
			return true;
		} else if (isGreaterThanEqualZero(value)) {
			//The following check is quicker than getting the numerator from a big int, and can get rid of a good number of
			// potential values, so we check it first
			return false;
		}
		return value.getNumerator().equals(Constants.FREE_BIG_INT_VALUE);
	}
}