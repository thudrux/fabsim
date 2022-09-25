package de.terministic.fabsim.core;

public interface ISimEvent {
	public AbstractComponent getComponent();

	public AbstractFlowItem getFlowItem();

	public int getPriority();

	public long getEventTime();

	public void resolveEvent();

	public void setEventTime(long l);

	public long getId();
}
