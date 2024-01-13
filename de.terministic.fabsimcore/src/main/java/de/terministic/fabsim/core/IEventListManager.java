package de.terministic.fabsim.core;

public interface IEventListManager {

	public ISimEvent getNextEvent();

	public void scheduleEvent(final ISimEvent event);

	public void unscheduleEvent(final ISimEvent event);

	public void setSimulationEndTime(long endTime);

	public int size();

	public long getSpentTime();

	public void setSpentTime(final long spentTime);

}
