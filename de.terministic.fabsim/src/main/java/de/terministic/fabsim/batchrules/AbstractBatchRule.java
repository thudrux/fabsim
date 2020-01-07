package de.terministic.fabsim.batchrules;

import java.util.ArrayList;

import de.terministic.fabsim.components.Batch;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.dispatchRules.AbstractDispatchRule;

public abstract class AbstractBatchRule extends AbstractModelElement {

	private String name;

	public AbstractBatchRule(FabModel model, final String name) {
		super(model);
		this.setName(name);
	}

	public abstract Batch choseBestBatch(ArrayList<Batch> items);

	public String getName() {
		return this.name;
	}

	public abstract QueueChangeAndBatches selectPossibleBatches(ArrayList<AbstractFlowItem> batch);

	public abstract QueueChangeAndBatches selectPossibleBatches(ArrayList<AbstractFlowItem> possibleItems,
			AbstractDispatchRule drule);

	public void setName(final String name) {
		this.name = name;
	}

	public abstract QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<AbstractFlowItem> list);

	public abstract QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<AbstractFlowItem> possibleItems,
			AbstractDispatchRule drule);
}
