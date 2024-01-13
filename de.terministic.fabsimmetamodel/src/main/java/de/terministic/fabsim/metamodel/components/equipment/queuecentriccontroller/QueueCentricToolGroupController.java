package de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Controller;
import de.terministic.fabsim.metamodel.components.ToolAndItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroupController;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.BreakdownFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.MaxWaitingTimeInQueueEvent;

public class QueueCentricToolGroupController extends AbstractToolGroupController {

	private final LinkedHashMap<ToolGroup, LinkedHashMap<String, IFlowItemQueue>> itemMap;

	public QueueCentricToolGroupController(FabModel model, Controller controller) {
		super(model, controller);
		itemMap = new LinkedHashMap<ToolGroup, LinkedHashMap<String, IFlowItemQueue>>();
	}

	// This should be part of the dispatch rule/queue
	@Deprecated
	private void createMaxWaitingTimeEvent(final AbstractFlowItem flowItem, final ToolGroup tg) {
		if (flowItem.getCurrentStep().getBatchDetails().getMaxWait() < Long.MAX_VALUE) {
			final MaxWaitingTimeInQueueEvent event = new MaxWaitingTimeInQueueEvent((FabModel) getModel(),
					this.getTime() + flowItem.getCurrentStep().getBatchDetails().getMaxWait(), tg, flowItem);
			this.getSimulationEngine().getEventList().scheduleEvent(event);
			flowItem.setMaxWaitEvent(event);
		}
	}

	private String checkIfItemHasBatchDetailsAndReturnThem(final AbstractFlowItem flowItem, final ToolGroup tg) {
		String r;
		if (flowItem.getCurrentStep().getBatchDetails() != null) {
			createMaxWaitingTimeEvent(flowItem, tg); // TODO this should be moved to the dispatch rule/queue
			r = flowItem.getCurrentStep().getBatchDetails().getBatchId();
		} else {
			r = this.NO_BATCH_BATCH_ID;
		}
		return r;
	}

	@Override
	public void addNewItem(AbstractFlowItem flowItem, ToolGroup tg) {
		String id;
		id = checkIfItemHasBatchDetailsAndReturnThem(flowItem, tg);
		logger.trace("Item {} has {} as batch id", flowItem, id);
		IFlowItemQueue queue = (IFlowItemQueue) getQueueForBatchId(tg, id);
		if (queue == null) {
			BatchDetails details = flowItem.getCurrentStep().getBatchDetails();
			if (details != null) {
				queue = controller.getDispatchRule(tg).createBatchQueue(details);
			} else {
				queue = controller.getDispatchRule(tg).createQueue();
			}
			this.itemMap.get(tg).put(id, queue);
		}
		queue.addFlowItem(flowItem);
	}

	@Override
	public void addNewToolGroup(ToolGroup toolGroup) {
		if (!this.itemMap.containsKey(toolGroup)) {
			this.itemMap.put(toolGroup, new LinkedHashMap<String, IFlowItemQueue>());
		}
	}

	@Override
	public List<AbstractFlowItem> canUnbatch(AbstractFlowItem flowItem) {
		List<AbstractFlowItem> ret = new ArrayList<>();
		if (flowItem instanceof Batch) {
			if (flowItem.getCurrentStepNumber() >= flowItem.getRecipe().size()) {
				ret = ((Batch) flowItem).getItems();
			} else {
				ret.add(flowItem);
			}
		} else {
			ret.add(flowItem);
		}
		return ret;
	}

	@Override
	public Collection<String> getBatchIdsForToolGroup(ToolGroup toolGroup) {
		return itemMap.get(toolGroup).keySet();
	}

	@Override
	public Collection<AbstractFlowItem> getQueueForBatchId(ToolGroup toolGroup, String batchId) {
		return itemMap.get(toolGroup).get(batchId);
	}

	@Override
	public Collection<ToolGroup> getToolGroups() {
		return itemMap.keySet();
	}

	@Override
	public void onBreakdownFinished(BreakdownFinishedEvent event) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	@Override
	public ToolAndItem selectToolAndItem(ToolGroup tg, AbstractFlowItem item) {
		ToolAndItem result = null;
		for (final AbstractTool t : tg.getStandbyTools()) {
			if (!t.canProcessItem()) {
				break;
			}
			if (!tg.getSetupStrategy().filterForValidItem(t, item)) {
				break;
			}
			result = selectToolAndItem(tg, t);
			if (result != null) {
				break;
			}
		}
		return result;
	}

	private IFlowItemQueue getValidFlowItemList(ToolGroup tg) {
		IFlowItemQueue queue;
		queue = this.getController().getDispatchRule(tg).createQueue();
		if (this.itemMap.get(tg).containsKey(NO_BATCH_BATCH_ID)) {
			for (AbstractFlowItem item : this.itemMap.get(tg).get(NO_BATCH_BATCH_ID)) {
				queue.addFlowItemCandidate(item, this.itemMap.get(tg).get(NO_BATCH_BATCH_ID));
			}
		} else {
			for (IFlowItemQueue q : itemMap.get(tg).values()) {
				AbstractFlowItem candidate = q.lookAtHighestPriorityFlowItem();
				if (candidate != null) {
					queue.addFlowItemCandidate(candidate, q);
				}
			}
		}
		return queue;
	}

	@Override
	public ToolAndItem selectToolAndItem(ToolGroup tg, AbstractTool tool) {
		this.logger.trace("Starting to select item for tool in ToolGroup");
		ToolAndItem result = null;
		if (tool.canProcessItem()) {
			final AbstractDispatchRule drule = this.getController().getDispatchRule(tg);
			IFlowItemQueue possibleItems = getValidFlowItemList(tg);
			if (tg.isConsidersDedication()) {
				tool.dedicationFilter(possibleItems);
			}
			this.logger.trace("possible items :{} ", possibleItems);
			tg.getSetupStrategy().filterValidItems(tool, possibleItems);
			this.logger.trace("items with fitting setup : {}", possibleItems);
			if (possibleItems.size() > 0) {
				final AbstractFlowItem item = possibleItems.takeBestCandidate();
				result = new ToolAndItem(tool, item);
			}
		}
		return result;
	}

}
