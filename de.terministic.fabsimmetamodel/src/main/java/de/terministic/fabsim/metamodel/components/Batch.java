package de.terministic.fabsim.metamodel.components;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;

public class Batch extends AbstractFlowItem {

	private ArrayList<AbstractFlowItem> items = new ArrayList<>();

	public Batch(final FabModel model, final Recipe recipe) {
		super(model);
		this.recipe = recipe;
		setType(FlowItemType.BATCH);

	}

	public void addItem(final AbstractFlowItem item) {
		this.items.add(item);
		size += item.getSize();
	}

	public boolean contains(final AbstractFlowItem item) {
		return this.items.contains(item);
	}

	public ArrayList<AbstractFlowItem> getItems() {
		return this.items;
	}

	private int size = 0;

	@Override
	public int getSize() {
		return size;
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
		size = 0;
		for (AbstractFlowItem item : items) {
			size += item.getSize();
		}
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
