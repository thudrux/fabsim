package de.terministic.fabsim.benchmarks.minifab;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class MiniFabProducts {

	static final double FLOW_FACTOR = 2.5;

	static final ProductSpec[] PRODUCTS = new ProductSpec[] {
			new ProductSpec(MiniFab.PRODUCT_PA, minutes(225), 1, 1,
					new ProductProcessingTimes(minutes(225), minutes(18), minutes(70), minutes(34), minutes(255),
							minutes(16)),
					FLOW_FACTOR),
			new ProductSpec(MiniFab.PRODUCT_PB, minutes(381), 1, 1,
					new ProductProcessingTimes(minutes(225), minutes(30), minutes(55), minutes(50), minutes(255),
							minutes(10)),
					FLOW_FACTOR),
			new ProductSpec(MiniFab.PRODUCT_TW, minutes(4567), 1, 1,
					new ProductProcessingTimes(minutes(225), minutes(42), minutes(40), minutes(66), minutes(255),
							minutes(4)),
					FLOW_FACTOR) };

	static final Map<Integer, Integer> PRIORITY_WEIGHTS = createPriorityWeights();

	private MiniFabProducts() {
	}

	private static long minutes(final long minutes) {
		return minutes * MiniFab.MINUTE;
	}

	private static Map<Integer, Integer> createPriorityWeights() {
		final Map<Integer, Integer> weights = new LinkedHashMap<>();
		for (final ProductSpec product : PRODUCTS) {
			weights.put(product.priority, product.priorityWeight);
		}
		return Collections.unmodifiableMap(weights);
	}

	static final class ProductSpec {
		final String name;
		final long interarrivalTimeMean;
		final int priority;
		final int priorityWeight;
		final long dueDateLeadTime;
		final ProductProcessingTimes processingTimes;

		private ProductSpec(final String name, final long interarrivalTimeMean, final int priority,
				final int priorityWeight, final ProductProcessingTimes processingTimes, final double flowFactor) {
			this.name = name;
			this.interarrivalTimeMean = interarrivalTimeMean;
			this.priority = priority;
			this.priorityWeight = priorityWeight;
			this.processingTimes = processingTimes;
			this.dueDateLeadTime = Math.round(processingTimes.getTotalProcessDuration() * flowFactor);
		}
	}

	static final class ProductProcessingTimes {
		final long s1;
		final long s2;
		final long s3;
		final long s4;
		final long s5;
		final long s6;

		private ProductProcessingTimes(final long s1, final long s2, final long s3, final long s4, final long s5,
				final long s6) {
			this.s1 = s1;
			this.s2 = s2;
			this.s3 = s3;
			this.s4 = s4;
			this.s5 = s5;
			this.s6 = s6;
		}

		private long getTotalProcessDuration() {
			return this.s1 + this.s2 + this.s3 + this.s4 + this.s5 + this.s6;
		}
	}
}
