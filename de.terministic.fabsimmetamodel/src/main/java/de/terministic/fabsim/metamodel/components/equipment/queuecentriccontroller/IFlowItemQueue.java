package de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller;

import java.util.Collection;

import de.terministic.fabsim.metamodel.AbstractFlowItem;

public interface IFlowItemQueue extends Collection<AbstractFlowItem> {

	public int size();

	public int sizeInProcessUnits();

	public int sizeInWafers();

	public AbstractFlowItem takeHighestPriorityFlowItem();

	public AbstractFlowItem lookAtHighestPriorityFlowItem();

	public boolean addFlowItem(AbstractFlowItem item);

	public boolean addFlowItemCandidate(AbstractFlowItem item, IFlowItemQueue queue);

	public AbstractFlowItem takeBestCandidate();

	public AbstractFlowItem takeCandidate(AbstractFlowItem item);

}
