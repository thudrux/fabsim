package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

abstract class FlowItemSnapshotBase {
	private final int priority;
	private final long lateness;
	private final String recipe;

	FlowItemSnapshotBase(final int priority, final long lateness, final String recipe) {
		this.priority = priority;
		this.lateness = lateness;
		this.recipe = recipe;
	}

	public int getPriority() {
		return this.priority;
	}

	public long getLateness() {
		return this.lateness;
	}

	public String getRecipe() {
		return this.recipe;
	}
}
