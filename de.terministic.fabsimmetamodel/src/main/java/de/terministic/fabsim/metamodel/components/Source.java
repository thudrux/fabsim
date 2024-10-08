package de.terministic.fabsim.metamodel.components;

import java.util.HashSet;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.AbstractSource;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.equipment.InvalidEventForResourceException;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;

public class Source extends AbstractSource {
	private Recipe recipe;

	protected IValue interArrivalTime;
	private boolean createFirstAtTimeZero = false;

	private Product product;

	protected HashSet<CreationEvent> outstandingEvents = new HashSet<>();
	private int releaseSize = 1;
	protected int lotSize = 1;

	public Source(FabModel model, final String name) {
		super(model, name);
	}

	@Override
	public void addBreakdown(final IBreakdown breakdown) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	@Override
	public boolean canProcessItem() {
		throw new NotYetImplementedException();
	}

	protected void createAndScheduleNextCreationEvent(final Product product, final long nextCreationTime) {
		for (int i = 0; i < this.releaseSize; i++) {
			final CreationEvent newEvent = new CreationEvent((FabModel) getModel(), nextCreationTime, this, product);
			getSimulationEngine().getEventList().scheduleEvent(newEvent);
			this.outstandingEvents.add(newEvent);
		}
	}

	public AbstractFlowItem generateFlowItemOfProduct(final CreationEvent event, final Product product) {
		logger.debug("generateFlowItemwithRecipe was called");
		final BasicFlowItem flowItem = new BasicFlowItem((FabModel) getSimulationEngine().getModel(), product);
		this.outstandingEvents.remove(event);
		if (this.outstandingEvents.size() == 0) {
			final long nextCreationTime = getSimulationEngine().getTime() + this.interArrivalTime.getValue();
			createAndScheduleNextCreationEvent(product, nextCreationTime);
		}
		sendFlowItemToResource(flowItem, ((FabModel) getModel()).getRouting());
		return flowItem;
	}

	public IValue getInterArrivalTime() {
		return this.interArrivalTime;
	}

	public String getProductName() {
		return this.product.getName();
	}

	// @Override
	// public Recipe getRecipe() {
	// return this.recipe;
	// }

	public int getReleaseSize() {
		return this.releaseSize;
	}

	@Override
	public void initialize() {
		if (this.createFirstAtTimeZero) {
			createAndScheduleNextCreationEvent(this.product, 0L);
		} else {
			createAndScheduleNextCreationEvent(this.product, this.interArrivalTime.getValue());
		}
	}

	public boolean isCreateFirstAtTimeZero() {
		return this.createFirstAtTimeZero;
	}

	public void resolveEvent(final ISimEvent event) {
		event.resolveEvent();
	}

	public void setCreateFirstAtTimeZero(final boolean createFirstAtTimeZero) {
		this.createFirstAtTimeZero = createFirstAtTimeZero;
	}

	public void setInterArrivalTime(final IValue interArrivalTime) {
		this.interArrivalTime = interArrivalTime;

	}

	public void setProduct(final Product product) {
		this.product = product;
	}

	// public void setRecipe(final Recipe recipe) {
	// this.recipe = recipe;
	// }

	public void setReleaseSize(final int releaseSize) {
		this.releaseSize = releaseSize;
	}

	@Override
	public void announceFlowItemArrival(AbstractFlowItem item) {
		throw new InvalidEventForResourceException(
				"Component does not support arriving flow items " + this.getClass().getSimpleName());
	}

	@Override
	public long getAvgInterarrivalTime() {
		return interArrivalTime.getAvgValue();
	}

	@Override
	public Product getProduct() {
		// TODO Auto-generated method stub
		return product;
	}

}
