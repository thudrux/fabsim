package de.terministic.fabsim.core.duration;

import java.util.Random;
import java.util.TreeMap;

import de.terministic.fabsim.core.IModel;

public class DurationFactory {

	private final TreeMap<Long, ConstantValue> constMap = new TreeMap<>();
	private final Random random;

	public DurationFactory(final IModel model) {
		this(model, null);
	}

	public DurationFactory(final IModel model, final Random random) {
		this.random = random;
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
		return new ExponentialDuration(mean, createRandom());
	}

	public ExponentialDuration createExponentialValueObject(final long mean, Random rand) {
		final ExponentialDuration result = new ExponentialDuration(mean, rand);
		return result;
	}

	public UniformDuration createUniformValueObject(final long min, final long max) {
		return new UniformDuration(min, max, createRandom());
	}

	public UniformDuration createUniformValueObject(final long min, final long max, Random rand) {
		final UniformDuration result = new UniformDuration(min, max, rand);
		return result;
	}

	private Random createRandom() {
		if (this.random == null) {
			return new Random();
		}
		return new Random(this.random.nextLong());
	}
}
