package de.terministic.fabsim.benchmarks.results;

final class SummaryStatistics {
	final double mean;
	final double stdDev;

	SummaryStatistics(final double mean, final double stdDev) {
		this.mean = mean;
		this.stdDev = stdDev;
	}

	static SummaryStatistics of(final double[] values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("values must not be empty");
		}
		double sum = 0.0d;
		for (final double value : values) {
			sum += value;
		}
		final double mean = sum / values.length;
		double variance = 0.0d;
		if (values.length > 1) {
			for (final double value : values) {
				final double delta = value - mean;
				variance += delta * delta;
			}
			variance = variance / (values.length - 1);
		}
		return new SummaryStatistics(mean, Math.sqrt(Math.max(0.0d, variance)));
	}
}
