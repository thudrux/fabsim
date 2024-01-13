package de.terministic.fabsim.metamodel.batchrules;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;

public class QueueChangeAndBatches {

	private ArrayList<AbstractFlowItem> queue;
	private ArrayList<AbstractFlowItem> batches;
	private AbstractFlowItem batch;

	public QueueChangeAndBatches(ArrayList<AbstractFlowItem> queue, ArrayList<AbstractFlowItem> batches) {
		this.queue = queue;
		this.batches = batches;
	}

	public QueueChangeAndBatches(ArrayList<AbstractFlowItem> queue, AbstractFlowItem batch) {
		this.queue = queue;
		this.setBatch(batch);
	}

	public ArrayList<AbstractFlowItem> getQueue() {
		return queue;
	}

	protected void setQueue(ArrayList<AbstractFlowItem> queue) {
		this.queue = queue;
	}

	public ArrayList<AbstractFlowItem> getBatches() {
		return batches;
	}

	protected void setBatches(ArrayList<AbstractFlowItem> batches) {
		this.batches = batches;
	}

	public AbstractFlowItem getBatch() {
		return batch;
	}

	public void setBatch(AbstractFlowItem batch) {
		this.batch = batch;
	}

}
