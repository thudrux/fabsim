package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class BufferFinishedEvent extends AbstractSimEvent {

	public BufferFinishedEvent(FabModel model, long time, CoolDownBuffer buffer, AbstractFlowItem flowItem) {
		super(model, time, buffer, flowItem);
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((CoolDownBuffer) getComponent()).onProcessFinished(this);
	}

	@Override
	public int getPriority() {
		return 8;
	}

}
