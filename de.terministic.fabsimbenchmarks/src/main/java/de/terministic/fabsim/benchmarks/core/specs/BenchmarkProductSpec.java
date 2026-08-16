package de.terministic.fabsim.benchmarks.core.specs;

import de.terministic.fabsim.core.duration.IValue;

public final class BenchmarkProductSpec {
	private final String name;
	private final int priority;
	private final IValue releaseDistribution;
	private final long dueDateLeadTime;
	private final BenchmarkRouteSpec route;

	public BenchmarkProductSpec(final String name, final int priority, final IValue releaseDistribution,
			final long dueDateLeadTime, final BenchmarkRouteSpec route) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name must not be blank");
		}
		if (releaseDistribution == null) {
			throw new IllegalArgumentException("releaseDistribution must not be null");
		}
		if (route == null) {
			throw new IllegalArgumentException("route must not be null");
		}
		this.name = name;
		this.priority = priority;
		this.releaseDistribution = releaseDistribution;
		this.dueDateLeadTime = dueDateLeadTime;
		this.route = route;
	}

	public String getName() {
		return this.name;
	}

	public int getPriority() {
		return this.priority;
	}

	public IValue getReleaseDistribution() {
		return this.releaseDistribution;
	}

	public long getDueDateLeadTime() {
		return this.dueDateLeadTime;
	}

	public BenchmarkRouteSpec getRoute() {
		return this.route;
	}
}
