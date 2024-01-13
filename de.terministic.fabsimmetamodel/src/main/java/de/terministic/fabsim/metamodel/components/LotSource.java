package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;

public class LotSource extends Source {
	private boolean allowSplit = false;

	public LotSource(FabModel model, final String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractFlowItem generateFlowItemOfProduct(final CreationEvent event, final Product product) {
		final Lot flowItem = new Lot((FabModel) getModel(), product, getLotSize(), 1, Long.MAX_VALUE);
		flowItem.setupForSimulation(getSimulationEngine());
		flowItem.setCreationTime(getTime());
		this.outstandingEvents.remove(event);
		if (this.outstandingEvents.size() == 0) {
			final long nextCreationTime = getSimulationEngine().getTime() + this.interArrivalTime.getDuration();
			createAndScheduleNextCreationEvent(product, nextCreationTime);
		}
		sendFlowItemToResource(flowItem, ((FabModel) getModel()).getRouting());
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

	public String toString() {
		return this.getName() + "_" + this.getProduct() + "_" + this.getAvgInterarrivalTime();
	}

}
