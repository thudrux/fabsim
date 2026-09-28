package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import java.util.List;

public final class CostSnapshot {
	private final long totalProjectedTardiness;
	private final long workInProgress;

	CostSnapshot(final long totalProjectedTardiness, final long workInProgress) {
		this.totalProjectedTardiness = totalProjectedTardiness;
		this.workInProgress = workInProgress;
	}

	public static CostSnapshot capture(final List<ToolGroupSnapshot> toolGroups) {
		long totalProjectedTardiness = 0L;
		long workInProgress = 0L;
		if (toolGroups != null) {
			for (final ToolGroupSnapshot toolGroup : toolGroups) {
				totalProjectedTardiness += toolGroup.getTotalProjectedTardiness();
				workInProgress += toolGroup.getWorkInProgress();
			}
		}
		return new CostSnapshot(totalProjectedTardiness, workInProgress);
	}

	public long getTotalProjectedTardiness() {
		return this.totalProjectedTardiness;
	}

	public long getWorkInProgress() {
		return this.workInProgress;
	}
}
