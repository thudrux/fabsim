package de.terministic.fabsim.core.duration;

import java.util.Random;

public class ExponentialDurationObject implements IDuration {

	long mean;
	Random rand;

	// public ExponentialDurationObject(final long mean) {
	// this.mean = mean;
	// this.rand = new Random();
	// }

	public ExponentialDurationObject(final long mean, Random random) {
		this.mean = mean;
		this.rand = random;
	}

	@Override
	public long getDuration() {
		return Math.round(Math.log(1.0 - this.rand.nextDouble()) * (-this.mean));
	}

	@Override
	public long getAvgDuration() {
		return mean;
	}
}
