package de.terministic.fabsim.benchmarks.core.specs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BenchmarkRouteSpec {
	private final List<BenchmarkRouteStepSpec> steps;

	public BenchmarkRouteSpec(final List<BenchmarkRouteStepSpec> steps) {
		if (steps == null || steps.isEmpty()) {
			throw new IllegalArgumentException("steps must not be empty");
		}
		final ArrayList<BenchmarkRouteStepSpec> copy = new ArrayList<>(steps.size());
		for (final BenchmarkRouteStepSpec step : steps) {
			if (step == null) {
				throw new IllegalArgumentException("steps must not contain null entries");
			}
			copy.add(step);
		}
		this.steps = Collections.unmodifiableList(copy);
	}

	public static BenchmarkRouteSpec of(final BenchmarkRouteStepSpec... steps) {
		return new BenchmarkRouteSpec(Arrays.asList(steps));
	}

	public List<BenchmarkRouteStepSpec> getSteps() {
		return this.steps;
	}
}
