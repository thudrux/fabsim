package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;

public class ToolAndItem {

	private AbstractTool t;
	private AbstractFlowItem item;

	public ToolAndItem(final AbstractTool tool, final AbstractFlowItem item) {
		this.setTool(tool);
		this.setItem(item);
	}

	public AbstractFlowItem getItem() {
		return this.item;
	}

	public AbstractTool getTool() {
		return this.t;
	}

	public void setItem(final AbstractFlowItem item) {
		this.item = item;
	}

	public void setTool(final AbstractTool t) {
		this.t = t;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "_" + this.item.toString() + "_" + this.t.toString();
	}
}
