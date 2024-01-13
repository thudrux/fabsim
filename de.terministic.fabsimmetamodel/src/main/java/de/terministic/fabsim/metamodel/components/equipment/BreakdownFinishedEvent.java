package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class BreakdownFinishedEvent extends AbstractSimEvent {
	private final IBreakdown breakdown;

	public BreakdownFinishedEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
		this.breakdown = null;
	}

	public BreakdownFinishedEvent(FabModel model, final long time, final AbstractResource component,
			final IBreakdown breakdown) {
		super(model, time, component, null);
		this.breakdown = breakdown;
	}

	public IBreakdown getBreakdown() {
		return this.breakdown;
	}

	@Override
	public int getPriority() {
		return 9;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((AbstractResource) getComponent()).onBreakdownFinished(this);
	}
}
