package de.terministic.fabsim.core;

public abstract class AbstractSimEvent extends AbstractModelElement implements ISimEvent, Comparable<AbstractSimEvent> {
	protected AbstractComponent component;
	protected AbstractFlowItem item;

	private long time;

	public AbstractSimEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model);
		this.time = time;
		this.component = component;
		this.item = flowItem;
		// logger.debug("Event created {}", this);
	}

	@Override
	public int compareTo(final AbstractSimEvent other) {
		int result = Long.compare(this.getEventTime(), other.getEventTime());
		if (result == 0) {
			result = Integer.compare(this.getPriority(), other.getPriority());
		}
		if (result == 0) {
			result = Long.compare(this.getId(), other.getId());
		}
		return result;
	}

	@Override
	public AbstractComponent getComponent() {
		return this.component;
	}

	@Override
	public AbstractFlowItem getFlowItem() {
		return this.item;
	}

	// @Override
	public long getEventTime() {
		return this.time;
	}

	@Override
	public void setEventTime(final long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "_" + this.getId() + " " + this.time + " " + this.component + " "
				+ this.item;
	}

}
