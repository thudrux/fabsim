package de.terministic.fabsim.metamodel.externaldispatch;

public interface DispatchDecisionClient {
	long selectFlowItem(DispatchDecisionSnapshot snapshot);
}
