package de.terministic.fabsim.components;

import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class CreationEvent extends AbstractSimEvent {
	private Product product;

	public CreationEvent(FabModel model, long time, Source source, Product product) {
		super(model, time, source, null);
		this.product = product;
	}

	@Override
	public int getPriority() {
		return 10;
	}

	@Override
	public void resolveEvent() {
		AbstractFlowItem newItem = ((Source) getComponent()).generateFlowItemOfProduct(this, product);
		setFlowItem(newItem);
	}

	public void setFlowItem(final AbstractFlowItem flowItem) {
		this.item = flowItem;

	}

}
