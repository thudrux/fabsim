package de.terministic.fabsim.components.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import de.terministic.fabsim.batchrules.AbstractBatchRule;
import de.terministic.fabsim.batchrules.QueueChangeAndBatches;
import de.terministic.fabsim.components.Batch;
import de.terministic.fabsim.components.Controller;
import de.terministic.fabsim.components.ToolAndItem;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractFlowItem.FlowItemType;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.dispatchRules.MaxWaitingTimeInQueueEvent;

public class ToolGroupController extends AbstractToolGroupController {

	private final LinkedHashMap<ToolGroup, LinkedHashMap<String, ArrayList<AbstractFlowItem>>> itemMap;

	public ToolGroupController(final FabModel model, final Controller controller) {
		super(model, controller);
		this.itemMap = new LinkedHashMap<>();

	}

	// add items to the hashMap, key is the batchID
	@Override
	public void addNewItem(final AbstractFlowItem flowItem, final ToolGroup tg) {
		String id;
		id = checkIfItemHasBatchDetailsAndReturnThem(flowItem, tg);
		logger.trace("Item {} has {} as batch id", flowItem, id);
		// if (!this.itemMap.containsKey(tg)) {
		// logger.error("Tool was not initialized");
		// addNewToolGroup(tg);
		// }
		if (!this.itemMap.get(tg).containsKey(id)) {
			final ArrayList<AbstractFlowItem> list = new ArrayList<>();
			this.itemMap.get(tg).put(id, list);
		}
		controller.getDispatchRule(tg).addItemToList(flowItem, this.itemMap.get(tg).get(id));
	}

	@Override
	public void addNewToolGroup(final ToolGroup toolGroup) {
		if (!this.itemMap.containsKey(toolGroup)) {
			this.itemMap.put(toolGroup, new LinkedHashMap<String, ArrayList<AbstractFlowItem>>());
		}
	}

	@Override
	public List<AbstractFlowItem> canUnbatch(final AbstractFlowItem flowItem) {
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

	private String checkIfItemHasBatchDetailsAndReturnThem(final AbstractFlowItem flowItem, final ToolGroup tg) {
		String r;
		if (flowItem.getCurrentStep().getBatchDetails() != null) {
			createMaxWaitingTimeEvent(flowItem, tg);
			r = flowItem.getCurrentStep().getBatchDetails().getBatchId();
		} else {
			r = this.NO_BATCH_BATCH_ID;
		}
		return r;
	}

	private void createMaxWaitingTimeEvent(final AbstractFlowItem flowItem, final ToolGroup tg) {
		if (flowItem.getCurrentStep().getBatchDetails().getMaxWait() < Long.MAX_VALUE) {
			final MaxWaitingTimeInQueueEvent event = new MaxWaitingTimeInQueueEvent(getModel(),
					this.getTime() + flowItem.getCurrentStep().getBatchDetails().getMaxWait(), tg, flowItem);
			this.getSimulationEngine().getEventList().scheduleEvent(event);
			flowItem.setMaxWaitEvent(event);
		}
	}

	private String getBatchIDFromItem(final AbstractFlowItem item) {
		if (item.getCurrentStep().getBatchDetails() != null)
			return item.getCurrentStep().getBatchDetails().getBatchId();
		else
			return this.NO_BATCH_BATCH_ID;
	}

	@Override
	public Collection<ToolGroup> getToolGroups() {
		return itemMap.keySet();
	}

	@Override
	public Collection<String> getBatchIdsForToolGroup(ToolGroup toolGroup) {
		return itemMap.get(toolGroup).keySet();
	}

	@Override
	public Collection<AbstractFlowItem> getQueueForBatchId(ToolGroup toolGroup, String batchId) {
		return itemMap.get(toolGroup).get(batchId);
	}

//	@Override
//	public LinkedHashMap<ToolGroup, LinkedHashMap<String, ArrayList<AbstractFlowItem>>> getItemMap() {
//		return this.itemMap;
//	}

	protected ArrayList<AbstractFlowItem> getPossibleItemsForTheTool(final ToolGroup tg, final AbstractTool tool) {
		logger.trace("getPossibleItemsForTheTool Start: {}", tg);
		ArrayList<AbstractFlowItem> items = new ArrayList<>();
		final AbstractBatchRule brule = this.getController().getBatchRule(tg);
		final AbstractDispatchRule drule = this.getController().getDispatchRule(tg);
		for (final String batchId : this.itemMap.get(tg).keySet()) {
			if (this.itemMap.get(tg).get(batchId).size() > 0) {
				if (tool.canProcessItem(this.itemMap.get(tg).get(batchId).get(0))) {
					if (tg.getProcessingType().equals(ProcessingType.BATCH)) {
						logger.trace("getPossibleItemsForTheTool:  beforeBatchRule getpossible: {}", itemMap);
						// QueueChangeAndBatches selectionResult = brule
						// .selectPossibleBatches(this.itemMap.get(tg).get(batchId),
						// drule);
						QueueChangeAndBatches selectionResult = brule
								.selectFirstPossibleBatch(this.itemMap.get(tg).get(batchId), drule);
						logger.trace("getPossibleItemsForTheTool:  queue: {}", selectionResult.getQueue());
						this.itemMap.get(tg).put(batchId, selectionResult.getQueue());
						if (selectionResult.getBatch() != null) {
							items.add(selectionResult.getBatch());
						}

						logger.trace("getPossibleItemsForTheTool:  afterBatchRule getpossible: {}", itemMap);
					} else {
						items = this.itemMap.get(tg).get(batchId);
					}
				}
			}
		}
		logger.trace("getPossibleItemsForTheTool result: {}", items);
		return items;
	}

	protected void removeItemFromItemMap(final AbstractFlowItem item, final ToolGroup tg) {
		logger.trace("removeItemFromItemMapBefore:  removing map looks like: {}", itemMap);
		logger.trace("removeItemFromItemMapRemoving:  Item {} from map", item);
		final String batchId = getBatchIDFromItem(item);
		if (item.getType() == FlowItemType.BATCH) {
			for (final AbstractFlowItem i : ((Batch) item).getItems()) {
				logger.trace("removeItemFromItemMap: SingleItem for Batch is removed: {}", i);
				this.itemMap.get(tg).get(batchId).remove(i);
			}
		} else {
			this.itemMap.get(tg).get(batchId).remove(item);
		}
		logger.trace("removeItemFromItemMap: After removing map looks like: {}", itemMap);
	}

	@Override
	public ToolAndItem selectToolAndItem(final ToolGroup tg, final AbstractFlowItem item) {
		ToolAndItem result = null;
		for (final AbstractTool t : tg.getStandbyTools()) {
			if (!t.canProcessItem(item)) {
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

	@Override
	public ToolAndItem selectToolAndItem(final ToolGroup tg, final AbstractTool tool) {
		this.logger.trace("Starting to select item for tool in ToolGroup");
		final AbstractDispatchRule drule = this.getController().getDispatchRule(tg);
		ToolAndItem result = null;
		ArrayList<AbstractFlowItem> possibleItems = getPossibleItemsForTheTool(tg, tool);
		if (tg.isConsidersDedication()) {
			possibleItems = tool.dedicationFilter(possibleItems);
		}
		this.logger.trace("possible items :{} ", possibleItems);
		possibleItems = tg.getSetupStrategy().filterValidItems(tool, possibleItems);
		this.logger.trace("items with fitting setup : {}", possibleItems);
		if (possibleItems.size() > 0) {
			final AbstractFlowItem item = drule.getBestItem(possibleItems);
			removeItemFromItemMap(item, tg);
			result = new ToolAndItem(tool, item);
		}
		return result;
	}

	@Override
	public void onBreakdownFinished(BreakdownFinishedEvent event) {
		// TODO Auto-generated method stub

	}

}
