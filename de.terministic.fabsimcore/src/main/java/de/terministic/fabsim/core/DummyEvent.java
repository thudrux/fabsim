package de.terministic.fabsim.core;

public class DummyEvent extends AbstractSimEvent {

	public DummyEvent(IModel model, long time, IComponent component, IFlowItem flowItem) {
		super(model, time, component, flowItem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

}
