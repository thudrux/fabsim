package de.terministic.fabsim.metamodel.externaldispatch;

public final class DispatchDecisionResponse {

	private final long selectedFlowItemId;

	public DispatchDecisionResponse(final long selectedFlowItemId) {
		this.selectedFlowItemId = selectedFlowItemId;
	}

	public static DispatchDecisionResponse of(final long selectedFlowItemId) {
		return new DispatchDecisionResponse(selectedFlowItemId);
	}

	public long getSelectedFlowItemId() {
		return this.selectedFlowItemId;
	}
}
