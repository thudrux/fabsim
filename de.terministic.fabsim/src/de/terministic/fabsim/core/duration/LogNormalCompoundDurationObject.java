package de.terministic.fabsim.core.duration;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public class LogNormalCompoundDurationObject extends AbstractDurationObject {

	long mean;
	long base;
	long multiplier;
	LogNormalDistribution rand;
	double factor;

	public LogNormalCompoundDurationObject(final long mean, LogNormalDistribution random, long base, long multiplier,
			double factor) {
		this.mean = mean;
		this.rand = random;
		this.base = base;
		this.multiplier = multiplier;
		this.factor = factor;
	}

	@Override
	public long getDuration() {

		return (long) (factor * multiplier * (rand.sample() + base));
	}

	@Override
	public long getAvgDuration() {
		return mean;
	}
}
