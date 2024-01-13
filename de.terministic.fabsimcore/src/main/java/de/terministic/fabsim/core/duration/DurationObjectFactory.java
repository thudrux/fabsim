package de.terministic.fabsim.core.duration;

import java.util.Random;
import java.util.TreeMap;

import de.terministic.fabsim.core.IModel;

public class DurationObjectFactory {

	private final TreeMap<Long, ConstantDurationObject> constMap = new TreeMap<>();
	private final TreeMap<Long, ExponentialDurationObject> expMap = new TreeMap<>();

	public DurationObjectFactory(final IModel model) {
	}

	public ConstantDurationObject createConstantDurationObject(final long duration) {
		if (this.constMap.containsKey(duration))
			return this.constMap.get(duration);
		else {
			final ConstantDurationObject result = new ConstantDurationObject(duration);
			this.constMap.put(duration, result);
			return result;
		}
	}

	public ExponentialDurationObject createExponentialDurationObject(final long mean) {
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

	public ExponentialDurationObject createExponentialDurationObject(final long mean, Random rand) {
		if (this.expMap.containsKey(mean))
			return this.expMap.get(mean);
		else {
			final ExponentialDurationObject result = new ExponentialDurationObject(mean, rand);
			this.expMap.put(mean, result);
			return result;
		}
	}

}
