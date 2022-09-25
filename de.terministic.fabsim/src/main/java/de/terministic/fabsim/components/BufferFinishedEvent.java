package de.terministic.fabsim.components;

import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class BufferFinishedEvent extends AbstractSimEvent {

	public BufferFinishedEvent(FabModel model, long time, CoolDownBuffer buffer, AbstractFlowItem flowItem) {
		super(model, time, buffer, flowItem);
	}

	@Override
	public void resolveEvent() {
		((CoolDownBuffer) getComponent()).onProcessFinished(this);
	}

	@Override
	public int getPriority() {
		return 8;
	}

}
