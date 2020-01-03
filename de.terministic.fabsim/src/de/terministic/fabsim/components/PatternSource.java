package de.terministic.fabsim.components;

import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;

public class PatternSource extends Source {
	private boolean allowSplit = false;

	private int generationLoop = 0;

	public PatternSource(FabModel model, final String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractFlowItem generateFlowItemOfProduct(final CreationEvent event, final Product product) {
		final Lot flowItem = new Lot(getSimulationEngine().getModel(), product, getLotSize(), 1, Long.MAX_VALUE);
		flowItem.setCreationTime(getTime());
		this.outstandingEvents.remove(event);
		long nextCreationTime;
		if (this.outstandingEvents.size() == 0) {
			if (this.generationLoop == 0) {
				nextCreationTime = getSimulationEngine().getTime() + this.interArrivalTime.getDuration();
				this.generationLoop++;
			} else {
				if (this.generationLoop < 3) {
					nextCreationTime = getSimulationEngine().getTime();
					this.generationLoop++;
				} else {
					nextCreationTime = getSimulationEngine().getTime() + this.interArrivalTime.getDuration();
					this.generationLoop = 0;
				}
			}
			createAndScheduleNextCreationEvent(product, nextCreationTime);
		}
		getSimulationEngine().getEventFactory()
				.scheduleNewFlowItemArrivalEvent(getSimulationEngine().getModel().getRouting(), flowItem, this);

		return flowItem;

	}

	public int getLotSize() {
		return this.lotSize;
	}

	public boolean isAllowSplit() {
		return this.allowSplit;
	}

	public void setAllowSplit(final boolean allowSplit) {
		this.allowSplit = allowSplit;
	}

	public void setLotSize(final int lotSize) {
		this.lotSize = lotSize;
	}

}
