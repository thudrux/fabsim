package de.terministic.fabsim.core.eventlist.comparison;

import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.duration.IDuration;

public class HoldModelBlock extends AbstractResource {
	private IDuration duration;
	private int eventCount;

	public HoldModelBlock(FabModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize() {
		for (int i = 0; i < eventCount; i++) {
			this.getSimulationEngine().getEventList()
					.scheduleEvent(new HoldModelEvent(getModel(), getTime() + duration.getDuration(), this, null));
		}

	}

	@Override
	public void announceFlowItemArrival(AbstractFlowItem item) {
		// TODO Auto-generated method stub

	}

	public void resolveHoldEvent() {
		this.getSimulationEngine().getEventList()
				.scheduleEvent(new HoldModelEvent(getModel(), getTime() + duration.getDuration(), this, null));

	}

	public IDuration getDuration() {
		return duration;
	}

	public void setDuration(IDuration duration) {
		this.duration = duration;
	}

	public int getEventCount() {
		return eventCount;
	}

	public void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	@Override
	public void addBreakdown(IBreakdown breakdown) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canProcessItem() {
		// TODO Auto-generated method stub
		return true;
	}

}
