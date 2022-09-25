package de.terministic.fabsim.core;

public class DummyEvent extends AbstractSimEvent {

	public DummyEvent(FabModel model, long time, AbstractComponent component, AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resolveEvent() {
		// Do nothing

	}

}
