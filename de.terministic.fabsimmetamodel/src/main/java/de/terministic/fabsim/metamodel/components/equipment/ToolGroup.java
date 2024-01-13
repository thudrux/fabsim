/*
 *
 * @author    Falk Pappert
 */
package de.terministic.fabsim.metamodel.components.equipment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.FlowItemArrivalEvent;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.ToolAndItem;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.metamodel.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.metamodel.components.equipment.toolstatemachine.AbstractToolStateMachine;

/**
 * The Class ToolGroup.
 */
public class ToolGroup extends AbstractHomogeneousResourceGroup {

	/** The busy tools. */
	private Set<Tool> busyTools = new HashSet<>();

	/** The in process map. */
	private final Map<AbstractFlowItem, AbstractTool> inProcessMap = new LinkedHashMap<>();

	/** The queue. */
	private final List<AbstractFlowItem> queue = new ArrayList<>();

	/** The queue for finished items. */
	private final Map<Lot, Lot> finishedLot = new LinkedHashMap<>();

	/** The standby tools. */
	private List<Tool> standbyTools = new ArrayList<>();

	/**
	 * Instantiates a new tool group.
	 *
	 * @param name           the name of the tool group
	 * @param processingType the processing type
	 * @param number         the number of tools in the tool group
	 * @param stateMachine   the state machine which tools should follow
	 * @param tgController   the tg controller
	 */
	public ToolGroup(FabModel model, final String name, final ProcessingType processingType, final int number,
			final AbstractToolStateMachine stateMachine, final AbstractToolGroupController tgController,
			final AbstractSetupStrategy setupStrategy) {
		super(model, name, tgController);
		this.processingType = processingType;
		this.setupStrategy = setupStrategy;
		if (processingType.equals(ProcessingType.BATCH)) {
			this.batchDetails = new ArrayList<>();
		}
		this.setToolCount(number);
		this.tools = new LinkedHashMap<>();
		for (int i = 0; i < number; i++) {
			final Tool tool = new Tool(model, getName() + "_" + i, this, stateMachine); // TODO
																						// move
																						// tool
																						// generation
																						// to
																						// factory
			tool.setOpLoadPercentage(getOpLoadPercentage());
			tool.setOpProcessingPercentage(getOpProcessingPercentage());
			tool.setOpUnloadPercentage(getOpUnloadPercentage());
			tool.setSetupTransitions(this.setupTransitions);
			for (final IMaintenance maint : this.maintenanceMap.values()) {
				maint.addTool(tool);
			}
			this.tools.put(tool.getId(), tool);
			this.toolList.add(tool);
			this.standbyTools.add(tool);

		}
		tgController.addNewToolGroup(this);
	}

	@Override
	public void becomesAvailable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void becomesUnavailable() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canProcessItem() {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	private void checkIfItemIsLot(final List<IFlowItem> flowItems) {
		for (final IFlowItem item : flowItems) {
			if (item instanceof Lot) {
				if (((Lot) item).getCurrentLotSize() == ((Lot) item).getOriginalLotSize()) {
					sendFlowItemToResource(item, getModel().getRouting());
				} else {
					mergeLotIfPossibleOrPutItInQueue(item);
				}
			}
		}
	}

	/**
	 * Gets tools currently processing a flow item.
	 *
	 * @return the busy tools
	 */
	public Set<Tool> getBusyTools() {
		return this.busyTools;
	}

	@Override
	public List<AbstractResource> getGroupMembers() {
		return this.toolList;
	}

	/**
	 * Gets the map relating items to the tool they are processed on.
	 *
	 * @return the in process map
	 */
	public Map<AbstractFlowItem, AbstractTool> getInProcessMap() {
		return this.inProcessMap;
	}

	/**
	 * Gets the queue.
	 *
	 * @return the queue
	 */
	public List<AbstractFlowItem> getQueue() {
		return this.queue;
	}

	/**
	 * Gets the standby tools.
	 *
	 * @return the standby tools
	 */
	public List<Tool> getStandbyTools() {
		return this.standbyTools;
	}

	private void mergeLotIfPossibleOrPutItInQueue(final IFlowItem item) {
		Lot originalLot;
		if (((Lot) item).getOriginalLot() != null) {
			originalLot = ((Lot) item).getOriginalLot();
		} else {
			originalLot = ((Lot) item);
		}

		if (this.finishedLot.containsKey(originalLot)) {
			final Lot lot = ((Lot) item).mergeWithLot(this.finishedLot.get(originalLot));
			if (lot.getCurrentLotSize() == lot.getOriginalLotSize()) {
				this.finishedLot.remove(originalLot);
				if (originalLot.getMaxWaitEvent() != null) {
					getSimulationEngine().getEventList().unscheduleEvent(originalLot.getMaxWaitEvent());
					originalLot.setMaxWaitEvent(null);
				}
				sendFlowItemToResource(lot, getModel().getRouting());
			} else {
				this.finishedLot.put(originalLot, lot);
			}
		} else {
			this.finishedLot.put(originalLot, (Lot) item);
		}
	}

	@Override
	public void onResourceBecomesAvailable(final AbstractResource resource) {
		this.logger.trace("{} is available again", resource);
		if (!this.standbyTools.contains(resource)) {
			this.standbyTools.add((Tool) resource);
		}
		if (this.busyTools.contains(resource)) {
			this.busyTools.remove((Tool) resource);
		}
		ToolAndItem toolAndItem;
		toolAndItem = this.tgController.selectToolAndItem(this, (Tool) resource);
		if (toolAndItem != null) {
		}

		startFlowItemOnTool(toolAndItem);
	}

	@Override
	public void onResourceBecomesUnavailable(final AbstractResource resource) {
		if (this.standbyTools.contains(resource)) {
			this.standbyTools.remove(resource);
		}
		if (!this.busyTools.contains(resource)) {
			this.busyTools.add((Tool) resource);
		}
	}

	/**
	 * Sets the busy tools.
	 *
	 * @param busyTools the new busy tools
	 */
	public void setBusyTools(final Set<Tool> busyTools) {
		this.busyTools = busyTools;
	}

	/**
	 * Sets the standby tools.
	 *
	 * @param standbyTools the new standby tools
	 */
	public void setStandbyTools(final List<Tool> standbyTools) {
		this.standbyTools = standbyTools;
	}

	/**
	 * Sets the started tools.
	 *
	 * @param tools the new started tools
	 */
	public void setStartedTools(final List<Tool> tools) {
		this.busyTools.addAll(tools);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.core.AbstractModelElement#
	 * setupForSimulation(de.terministic.alternativefabsimulator.core.
	 * SimulationEngine)
	 */
	@Override
	public void setupForSimulation(final SimulationEngine engine) {
		super.setupForSimulation(engine);
		for (final AbstractTool tool : this.tools.values()) {
			tool.setupForSimulation(engine);
		}
	}

	/**
	 * Start flow item on tool.
	 *
	 * @param toolAndItem the tool and item
	 */
	private void startFlowItemOnTool(final ToolAndItem toolAndItem) {
		this.logger.trace("trying to start item on tool");
		if (toolAndItem != null) {
			final AbstractTool tool = toolAndItem.getTool();
			final AbstractFlowItem item = toolAndItem.getItem();
			item.getTimeStamps(item.getCurrentStepNumber()).setStartProcessingTime(this.tgController.getTime());
			this.queue.remove(item);
			this.busyTools.add((Tool) tool);
			this.standbyTools.remove((Tool) tool);
			this.inProcessMap.put(item, tool);
			this.logger.trace("Handing Item {} to tool {}", item, tool);
			item.unscheduleMaxQueueTimeEvents();
			sendFlowItemToResource(item, tool);

		}
	}

	@Override
	public void takeAfterProcessing(final AbstractFlowItem flowItem, final AbstractResource resource) {
		// this.logger.debug("START takeAfterProcessing");
		final AbstractTool abstractTool = (AbstractTool) resource;

		flowItem.markCurrentStepAsFinished();
		this.inProcessMap.remove(flowItem);
		// this.busyTools.remove(abstractTool);
		final List<IFlowItem> flowItems = this.tgController.canUnbatch(flowItem);
		checkIfItemIsLot(flowItems);
		// this.logger.debug("FINISH takeAfterProcessing");
	}

	public void tryToStartAfterMaxWaitingTime(final AbstractFlowItem item) {
		final ToolAndItem toolAndItem = this.tgController.selectToolAndItem(this, item);
		startFlowItemOnTool(toolAndItem);
	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		AbstractFlowItem flowItem = (AbstractFlowItem) event.getFlowItem();
		this.logger.trace("new flow item {} arrived at {}, {} tools are on standby(){}", flowItem, getName(),
				this.standbyTools.size(), this.standbyTools);
		flowItem.getTimeStamps(flowItem.getCurrentStepNumber()).setArrivalTime(this.tgController.getTime());
		this.queue.add(flowItem);
		this.tgController.addNewItem(flowItem, this);

		for (AbstractResource tool : getToolList()) {
			if (standbyTools.contains(tool)) {
				final ToolAndItem toolAndItem = this.tgController.selectToolAndItem(this, (Tool) tool);
				this.logger.trace("Tool and Item= {}", toolAndItem);
				startFlowItemOnTool(toolAndItem);
			}
		}
	}

	@Override
	public void announceFlowItemArrival(IFlowItem item) {
		// Do nothing tool group has no capa limit
	}
}
