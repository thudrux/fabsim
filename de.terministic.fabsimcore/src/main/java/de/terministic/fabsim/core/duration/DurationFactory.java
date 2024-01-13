package de.terministic.fabsim.core.duration;

import java.util.Random;
import java.util.TreeMap;

import de.terministic.fabsim.core.IModel;

public class DurationFactory {

	private final TreeMap<Long, ConstantDuration> constMap = new TreeMap<>();
	private final TreeMap<Long, ExponentialDuration> expMap = new TreeMap<>();

	public DurationFactory(final IModel model) {
	}

	public ConstantDuration createConstantDurationObject(final long duration) {
		if (this.constMap.containsKey(duration))
			return this.constMap.get(duration);
		else {
			final ConstantDuration result = new ConstantDuration(duration);
			this.constMap.put(duration, result);
			return result;
		}
	}

	public ExponentialDuration createExponentialDurationObject(final long mean) {
		throw new RuntimeException("ExponentialDistriWithoutRandomGen Given");
		// if (this.expMap.containsKey(mean))
		// return this.expMap.get(mean);
		// else {
		// final ExponentialDurationObject result = new
		// // ExponentialDurationObject(mean);
		// this.expMap.put(mean, result);
		// return result;
		// }
	}

	public ExponentialDuration createExponentialDurationObject(final long mean, Random rand) {
		if (this.expMap.containsKey(mean))
			return this.expMap.get(mean);
		else {
			final ExponentialDuration result = new ExponentialDuration(mean, rand);
			this.expMap.put(mean, result);
			return result;
		}
	}

}
