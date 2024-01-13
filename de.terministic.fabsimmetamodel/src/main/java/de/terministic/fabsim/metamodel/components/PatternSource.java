package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;

public class PatternSource extends Source {
	private boolean allowSplit = false;

	private int generationLoop = 0;

	public PatternSource(FabModel model, final String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractFlowItem generateFlowItemOfProduct(final CreationEvent event, final Product product) {
		final Lot flowItem = new Lot((FabModel) getModel(), product, getLotSize(), 1, Long.MAX_VALUE);
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
		((FabModel) getModel()).getEventFactory()
				.scheduleNewFlowItemArrivalEvent(((FabModel) getModel()).getRouting(), flowItem, this);

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
