package de.terministic.fabsim.core;

public class MinTimeSimulationEndCondition implements ISimEndCondition {

	private final long minTime;

	public MinTimeSimulationEndCondition(long minTime) {
		this.minTime = minTime;
	}

	@Override
	public boolean isConditionFulfilled(ISimEvent event) {
		return event.getEventTime() > minTime;
	}

}
