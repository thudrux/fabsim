package de.terministic.fabsim.metamodel.batchrules;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;

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

	public void setName(final String name) {
		this.name = name;
	}

	public abstract QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<AbstractFlowItem> list);

	public abstract QueueChangeAndBatches selectFirstPossibleBatch(ArrayList<AbstractFlowItem> possibleItems,
			AbstractDispatchRule drule);
}
