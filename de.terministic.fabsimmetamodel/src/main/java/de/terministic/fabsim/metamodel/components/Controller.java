package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.batchrules.AbstractBatchRule;
import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;

public class Controller extends AbstractModelElement {

	private AbstractDispatchRule globalDispatchRule;
	private final AbstractBatchRule globalBatchRule;

	public Controller(final FabModel fabModel, final AbstractDispatchRule globalRule,
			final AbstractBatchRule globalBatchRule) {
		super(fabModel);
		this.globalDispatchRule = globalRule;
		this.globalBatchRule = globalBatchRule;
	}

	public AbstractBatchRule getBatchRule(final AbstractHomogeneousResourceGroup tg) {
		AbstractBatchRule usedRule = this.globalBatchRule;
		if (tg.getBatchRule() != null) {
			usedRule = tg.getBatchRule();
		}
		return usedRule;
	}

	public AbstractDispatchRule getDispatchRule(final AbstractToolGroup tg) {
		AbstractDispatchRule usedRule = this.globalDispatchRule;
		if (tg.getDispatchRule() != null) {
			usedRule = tg.getDispatchRule();
		}
		return usedRule;
	}

	public AbstractDispatchRule getGlobalRule() {
		return this.globalDispatchRule;
	}

	public void setGlobalRule(final AbstractDispatchRule rule) {
		this.globalDispatchRule = rule;
	}
}
