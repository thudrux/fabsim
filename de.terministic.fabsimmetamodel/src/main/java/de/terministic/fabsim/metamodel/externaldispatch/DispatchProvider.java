package de.terministic.fabsim.metamodel.externaldispatch;

public interface DispatchProvider {
	DispatchDecisionResponse selectDispatchCandidate(DispatchDecisionRequest request);
}
