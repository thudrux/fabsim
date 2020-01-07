package de.terministic.fabsim.components;

import java.util.ArrayList;

import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;

public class Batch extends AbstractFlowItem {

	private ArrayList<AbstractFlowItem> items = new ArrayList<>();

	public Batch(final FabModel model, final Recipe recipe) {
		super(model);
		this.recipe = recipe;
		setType(FlowItemType.BATCH);

	}

	public void addItem(final AbstractFlowItem item) {
		this.items.add(item);
	}

	public boolean contains(final AbstractFlowItem item) {
		return this.items.contains(item);
	}

	public ArrayList<AbstractFlowItem> getItems() {
		return this.items;
	}

	@Override
	public int getSize() {
		int result = 0;
		for (final AbstractFlowItem item : this.items) {
			result += item.getSize();
		}
		return result;
	}

	@Override
	public void markCurrentStepAsFinished() {
		super.markCurrentStepAsFinished();
		for (final AbstractFlowItem item : this.items) {
			item.markCurrentStepAsFinished();
		}
	}

	public void setItems(final ArrayList<AbstractFlowItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return super.toString() + " " + getSize() + "(" + this.items + ")";
	}

	@Override
	public void unscheduleMaxQueueTimeEvents() {
		for (AbstractFlowItem item : items) {
			item.unscheduleMaxQueueTimeEvents();
		}
	}

}
