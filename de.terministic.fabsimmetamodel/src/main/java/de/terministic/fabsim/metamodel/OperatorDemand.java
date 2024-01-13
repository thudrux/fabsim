package de.terministic.fabsim.metamodel;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.toolstatemachine.ProcessStateDetails;

public class OperatorDemand {
	private final AbstractFlowItem item;
	private final AbstractTool tool;
	private final ProcessStateDetails details;

	public OperatorDemand(final AbstractTool tool, final AbstractFlowItem item, final ProcessStateDetails details) {
		this.details = details;
		this.item = item;
		this.tool = tool;
	}

	public ProcessStateDetails getDetails() {
		return this.details;
	}

	public AbstractFlowItem getItem() {
		return this.item;
	}

	public AbstractTool getTool() {
		return this.tool;
	}

	public void notifyOfFullfillment() {
		this.tool.onOperatorBecomesAvailable(this);

	}

}
