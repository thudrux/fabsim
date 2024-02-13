package de.terministic.fabsim.core.duration;

import java.util.Random;
import java.util.TreeMap;

import de.terministic.fabsim.core.IModel;

public class DurationFactory {

	private final TreeMap<Long, ConstantValue> constMap = new TreeMap<>();

	public DurationFactory(final IModel model) {
	}

	public ConstantValue createConstantValueObject(final long duration) {
		if (this.constMap.containsKey(duration))
			return this.constMap.get(duration);
		else {
			final ConstantValue result = new ConstantValue(duration);
			this.constMap.put(duration, result);
			return result;
		}
	}

	public ExponentialDuration createExponentialValueObject(final long mean) {
		 return new ExponentialDuration(mean, new Random());
	}

	public ExponentialDuration createExponentialValueObject(final long mean, Random rand) {
		final ExponentialDuration result = new ExponentialDuration(mean, rand);
		return result;
	}

}
