package de.terministic.fabsim.metamodel.batchrules;

import java.util.ArrayList;

import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.AbstractFlowItem;

public class QueueChangeAndBatches {

	private ArrayList<IFlowItem> queue;
	private ArrayList<IFlowItem> batches;
	private IFlowItem batch;

	public QueueChangeAndBatches(ArrayList<IFlowItem> queue, ArrayList<IFlowItem> batches) {
		this.queue = queue;
		this.batches = batches;
	}

	public QueueChangeAndBatches(ArrayList<IFlowItem> queue, IFlowItem batch) {
		this.queue = queue;
		this.setBatch(batch);
	}

	public ArrayList<IFlowItem> getQueue() {
		return queue;
	}

	protected void setQueue(ArrayList<IFlowItem> queue) {
		this.queue = queue;
	}

	public ArrayList<IFlowItem> getBatches() {
		return batches;
	}

	protected void setBatches(ArrayList<IFlowItem> batches) {
		this.batches = batches;
	}

	public IFlowItem getBatch() {
		return batch;
	}

	public void setBatch(IFlowItem batch) {
		this.batch = batch;
	}

}
