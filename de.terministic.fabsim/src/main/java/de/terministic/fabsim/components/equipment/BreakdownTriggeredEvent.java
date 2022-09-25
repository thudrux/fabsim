package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class BreakdownTriggeredEvent extends AbstractSimEvent {
	IBreakdown down;

	public BreakdownTriggeredEvent(FabModel model, final long time, final AbstractComponent component,
			final AbstractFlowItem flowItem) {
		super(model, time, component, flowItem);
		// TODO Auto-generated constructor stub
	}

	public BreakdownTriggeredEvent(FabModel model, final long time, final AbstractResource component,
			final IBreakdown breakdown) {
		super(model, time, component, null);
		this.down = breakdown;
	}

	public IBreakdown getBreakdown() {
		return this.down;
	}

	@Override
	public int getPriority() {
		return 5;
	}

	@Override
	public void resolveEvent() {
		((AbstractResource) this.component).onBreakdownTriggered(this);

	}

	public void setBreakdown(final IBreakdown down) {
		this.down = down;
	}

}
