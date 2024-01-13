package de.terministic.fabsim.core;

public abstract class AbstractSimEvent extends AbstractModelElement implements ISimEvent {
	protected IComponent component;
	protected IFlowItem item;

	private long time;

	public AbstractSimEvent(IModel model, final long time, final IComponent component,
			final IFlowItem flowItem) {
		super(model);
		this.time = time;
		this.component = component;
		this.item = flowItem;
		// logger.debug("Event created {}", this);
	}

	@Override
	public int compareTo(final ISimEvent other) {
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
	public IComponent getComponent() {
		return this.component;
	}

	@Override
	public IFlowItem getFlowItem() {
		return this.item;
	}

	// @Override
	public long getEventTime() {
		return this.time;
	}

	@Override
	public void setTime(final long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "_" + this.getId() + " " + this.time + " " + this.component + " "
				+ this.item;
	}

	@Override
	public void resolveEvent() {
		// logger.debug("[{}] Resolving Event {}", time, this);
	}

}
