package de.terministic.fabsim.metamodel.components;

import java.util.ArrayList;

import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.core.IModel;
import de.terministic.fabsim.metamodel.AbstractFlowItem;

public class Batch extends AbstractFlowItem {

	private ArrayList<IFlowItem> items = new ArrayList<>();

	public Batch(final IModel model, final Recipe recipe) {
		super(model);
		this.recipe = recipe;
		setType(FlowItemType.BATCH);

	}

	public void addItem(final IFlowItem item) {
		this.items.add(item);
		size += item.getSize();
	}

	public boolean contains(final AbstractFlowItem item) {
		return this.items.contains(item);
	}

	public ArrayList<IFlowItem> getItems() {
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
		for (final IFlowItem item : this.items) {
			((AbstractFlowItem)item).markCurrentStepAsFinished();
		}
	}

	public void setItems(final ArrayList<IFlowItem> items) {
		this.items = items;
		size = 0;
		for (IFlowItem item : items) {
			size += item.getSize();
		}
	}

	@Override
	public String toString() {
		return super.toString() + " " + getSize() + "(" + this.items + ")";
	}

	@Override
	public void unscheduleMaxQueueTimeEvents() {
		for (IFlowItem item : items) {
			((AbstractFlowItem)item).unscheduleMaxQueueTimeEvents();
		}
	}

}
