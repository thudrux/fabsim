package de.terministic.fabsim.benchmarks.core.specs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RouteSpec {
	private final List<RouteStepSpec> steps;

	public RouteSpec(final List<RouteStepSpec> steps) {
		if (steps == null || steps.isEmpty()) {
			throw new IllegalArgumentException("steps must not be empty");
		}
		final ArrayList<RouteStepSpec> copy = new ArrayList<>(steps.size());
		for (final RouteStepSpec step : steps) {
			if (step == null) {
				throw new IllegalArgumentException("steps must not contain null entries");
			}
			copy.add(step);
		}
		this.steps = Collections.unmodifiableList(copy);
	}

	public static RouteSpec of(final RouteStepSpec... steps) {
		return new RouteSpec(Arrays.asList(steps));
	}

	public List<RouteStepSpec> getSteps() {
		return this.steps;
	}
}
