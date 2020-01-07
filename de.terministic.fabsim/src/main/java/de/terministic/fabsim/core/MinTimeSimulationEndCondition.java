package de.terministic.fabsim.core;

public class MinTimeSimulationEndCondition extends AbstractSimEndCondition {

	private long currentTime = 0L;
	private final long minTime;

	public MinTimeSimulationEndCondition(long minTime) {
		this.minTime = minTime;
	}

	public void setCurrentTime(long time) {
		currentTime = time;
	}

	@Override
	public boolean conditionFulfilled() {
		return currentTime > minTime;
	}

}
