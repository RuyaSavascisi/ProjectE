package moze_intel.projecte.emc.generator;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Map;
import moze_intel.projecte.api.mapper.generator.IValueGenerator;
import org.apache.commons.math3.fraction.BigFraction;

/**
 * Composes another IValueGenerator, and truncates all fractional values towards 0.
 *
 * @param <T> The type we are generating values for
 */
public class BigFractionToLongGenerator<T> implements IValueGenerator<T, Long> {

	private final IValueGenerator<T, BigFraction> inner;

	public BigFractionToLongGenerator(IValueGenerator<T, BigFraction> inner) {
		this.inner = inner;
	}

	@Override
	public Object2LongMap<T> generateValues() {
		Map<T, BigFraction> innerResult = inner.generateValues();
		Object2LongMap<T> myResult = new Object2LongOpenHashMap<>();
		for (Map.Entry<T, BigFraction> entry : innerResult.entrySet()) {
			BigFraction value = entry.getValue();
			if (value.longValue() > 0) {
				myResult.put(entry.getKey(), value.longValue());
			}
		}
		return myResult;
	}
}