package de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller;

import java.util.Collection;

import de.terministic.fabsim.core.IFlowItem;

public interface IFlowItemQueue extends Collection<IFlowItem> {

	public int size();

	public int sizeInProcessUnits();

	public int sizeInWafers();

	public IFlowItem takeHighestPriorityFlowItem();

	public IFlowItem lookAtHighestPriorityFlowItem();

	public boolean addFlowItem(IFlowItem item);

	public boolean addFlowItemCandidate(IFlowItem item, IFlowItemQueue queue);

	public IFlowItem takeBestCandidate();

	public IFlowItem takeCandidate(IFlowItem item);

}
