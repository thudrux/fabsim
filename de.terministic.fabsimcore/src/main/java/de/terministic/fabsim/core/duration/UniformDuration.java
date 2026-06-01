package de.terministic.fabsim.core.duration;

import java.util.Random;

public class UniformDuration implements IValue {

	long min;
	long max;
	Random rand;

	public UniformDuration(final long min, final long max, Random random) {
		this.min = min;
		this.max = max;
		this.rand = random;
	}

	@Override
	public long getValue() {
		return this.min + (long) Math.floor(this.rand.nextDouble() * (this.max - this.min + 1L));
	}

	@Override
	public long getAvgValue() {
		return (this.min + this.max) / 2L;
	}
}
