package de.terministic.fabsim.core;

public interface IEventListManager {

	public AbstractSimEvent getNextEvent();

	public void scheduleEvent(final AbstractSimEvent event);

	public void unscheduleEvent(final AbstractSimEvent event);

	public void setSimulationEndTime(long endTime);

	public int size();

	public long getSpentTime();

	public void setSpentTime(final long spentTime);

}
