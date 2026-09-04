package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

public final class CostSnapshot {
	private final long totalProjectedTardiness;
	private final long workInProgress;

	CostSnapshot(final long totalProjectedTardiness, final long workInProgress) {
		this.totalProjectedTardiness = totalProjectedTardiness;
		this.workInProgress = workInProgress;
	}

	public long getTotalProjectedTardiness() {
		return this.totalProjectedTardiness;
	}

	public long getWorkInProgress() {
		return this.workInProgress;
	}
}
