package de.terministic.fabsim.core;

public interface ISimEvent extends Comparable<ISimEvent> {
	public IComponent getComponent();

	public IFlowItem getFlowItem();

	public int getPriority();

	public long getEventTime();

	public void resolveEvent();

	public void setTime(long l);

	public long getId();
}
