package de.terministic.fabsim.components.equipment.toolstatemachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.BreakdownFinishedEvent;
import de.terministic.fabsim.components.equipment.BreakdownTriggeredEvent;
import de.terministic.fabsim.components.equipment.LoadingFinishedEvent;
import de.terministic.fabsim.components.equipment.MaintenanceFinishedEvent;
import de.terministic.fabsim.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.components.equipment.OperatorFinishedEvent;
import de.terministic.fabsim.components.equipment.ProcessFinishedEvent;
import de.terministic.fabsim.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.components.equipment.SetupFinishedEvent;
import de.terministic.fabsim.components.equipment.UnloadingFinishedEvent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.EventComparator;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.OperatorDemand;

public class BasicToolStateMachine extends AbstractToolStateMachine {
	private final Set<AbstractToolState> states = new HashSet<>();
	protected Map<AbstractTool, AbstractToolState> currentStateMap;
	private AbstractToolState initialState;
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	protected Map<AbstractTool, ArrayList<AbstractSimEvent>> queuedEvents;

	public BasicToolStateMachine(final FabModel model) {
		this.currentStateMap = new LinkedHashMap<>();
		this.queuedEvents = new LinkedHashMap<>();

		final StandbyToolState sb = new StandbyToolState(model);
		this.states.add(sb);
		final LoadingToolState load = new LoadingToolState(model);
		this.states.add(load);
		final ProcessingToolState pr = new ProcessingToolState(model);
		this.states.add(pr);
		final UnloadingToolState unload = new UnloadingToolState(model);
		this.states.add(unload);
		final SetupToolState setup = new SetupToolState(model);
		this.states.add(setup);
		final MaintenanceToolState maint = new MaintenanceToolState(model);
		this.states.add(maint);
		final BreakdownToolState ud = new BreakdownToolState(model);
		this.states.add(ud);

		sb.setProcessingToolState(pr);
		sb.setLoadingToolState(load);
		sb.setSetupToolState(setup);
		sb.setMaintenanceToolState(maint);
		sb.setBreakdownToolState(ud);

		pr.setStandbyToolState(sb);
		pr.setBreakdownToolState(ud);
		pr.setUnloadingToolState(unload);

		setup.setProcessingToolState(pr);
		setup.setMaintenanceToolState(maint);
		setup.setBreakdownToolState(ud);

		maint.setStandbyToolState(sb);
		maint.setBreakdownToolState(ud);

		ud.setStandbyToolState(sb);
		ud.setMaintenanceToolState(maint);

		load.setBreakdownToolState(ud);
		load.setProcessingToolState(pr);

		unload.setStandbyToolState(sb);
		unload.setBreakdownToolState(ud);

		this.initialState = sb;

	}

	public void addState(final AbstractToolState state) {
		this.states.add(state);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#getCurrentSemiE10StateOfTool(de.
	 * terministic.alternativefabsimulator.components.equipment.AbstractTool)
	 */
	@Override
	public SemiE10EquipmentState getCurrentSemiE10StateOfTool(final AbstractTool abstractTool) {
		return this.currentStateMap.get(abstractTool).getSemiE10State(abstractTool);
	}

	public Map<AbstractTool, AbstractToolState> getCurrentStateMap() {
		return this.currentStateMap;
	}

	public AbstractToolState getInitialState() {
		return this.initialState;
	}

	public Map<AbstractTool, ArrayList<AbstractSimEvent>> getQueuedEvents() {
		return this.queuedEvents;
	}

	public HashMap<AbstractTool, ProcessStateDetails> getStateDetails(final AbstractTool tool) {
		return this.currentStateMap.get(tool).getStateDetails();
	}

	public Set<AbstractToolState> getStates() {
		return this.states;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#handleFlowItemArrival(de.terministic.
	 * alternativefabsimulator.components.equipment.AbstractTool,
	 * de.terministic.alternativefabsimulator.core.AbstractFlowItem)
	 */
	@Override
	public void handleFlowItemArrival(final AbstractTool tool, final AbstractFlowItem item) {
		this.logger.trace("[{}] onFlowItemArrival", tool.getTime());
		this.logger.trace("Tool: {}  Item: {}", tool, item);
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		this.logger.trace("Current State: {}", currentState);
		final AbstractToolState newState = currentState.onFlowItemArrival(tool, item);
		this.logger.trace("new state: {}", newState);
		updateStateAndStartNewStateWithItem(tool, item, newState);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onBreakdownFinished(de.terministic.
	 * alternativefabsimulator.components.equipment.BreakdownFinishedEvent)
	 */
	@Override
	public void onBreakdownFinished(final BreakdownFinishedEvent event) {
		this.logger.trace("[{}] onBreakdownFinishedEvent", event.getComponent().getTime());
		this.logger.trace("Tool: {}  Item: {}", event.getComponent(), event.getFlowItem());
		final AbstractTool tool = (AbstractTool) event.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		this.logger.trace("Current State: {}", currentState);
		final AbstractToolState newState = currentState.onBreakdownFinished(tool, event.getBreakdown());
		if (newState == currentState) {
			BreakdownToolState udState = (BreakdownToolState) newState;

			this.logger.trace("[{}] next breakdown is handled {}", tool.getTime(), newState);
			tool.setCurrentToolState(newState.getSemiE10State(tool));
			this.currentStateMap.put(tool, newState);

			udState.enterState(tool, udState.getNextBreakdownInStack(tool));
		}

		else {
			updateStateAndResumeNewStateWithEvent(tool, event, newState);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onBreakdownTriggered(de.terministic.
	 * alternativefabsimulator.components.equipment.BreakdownTriggeredEvent)
	 */
	@Override
	public void onBreakdownTriggered(final BreakdownTriggeredEvent event) {
		this.logger.trace("[{}] onBreakdownTriggeredEvent", event.getComponent().getTime());
		this.logger.trace("Tool: {}  Item: {}", event.getComponent(), event.getFlowItem());
		final AbstractTool tool = (AbstractTool) event.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		this.logger.trace("Current State: {}", currentState);
		final BreakdownToolState newState = (BreakdownToolState) currentState.onBreakdownTriggered(tool,
				event.getBreakdown());
		if (newState != null) {
			newState.setPreviousState(tool, currentState);
		}

		this.logger.trace("[{}] new state is {}", tool.getTime(), newState);
		if (newState != null) {
			tool.setCurrentToolState(newState.getSemiE10State(tool));
			this.currentStateMap.put(tool, newState);
			newState.enterState(tool, event);
		} else {
			if (this.currentStateMap.get(tool).needsEventForLater(event)) {
				this.queuedEvents.get(tool).add(event);
				this.logger.trace("Event stored for later use");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onLoadingFinished(de.terministic.
	 * alternativefabsimulator.components.equipment.LoadingFinishedEvent)
	 */
	@Override
	public void onLoadingFinished(final LoadingFinishedEvent loadingFinishedEvent) {
		final AbstractTool tool = (AbstractTool) loadingFinishedEvent.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		final AbstractToolState newState = currentState.onLoadingFinished(loadingFinishedEvent);
		updateStateAndStartNewStateWithItem(tool, loadingFinishedEvent.getFlowItem(), newState);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onMaintenanceFinished(de.terministic.
	 * alternativefabsimulator.components.equipment.MaintenanceFinishedEvent)
	 */
	@Override
	public void onMaintenanceFinished(final MaintenanceFinishedEvent event) {
		this.logger.trace("[{}] onMaintenanceFinishedEvent", event.getComponent().getTime());
		this.logger.trace("Tool: {}  Item: {}", event.getComponent(), event.getFlowItem());
		final AbstractTool tool = (AbstractTool) event.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		this.logger.trace("Current State: {}", currentState);
		final AbstractToolState newState = currentState.onMaintenanceFinished(tool, event.getMaintenance());

		updateStateAndStartNewStateWithEvent(tool, event, newState);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onMaintenanceTriggered(de.terministic.
	 * alternativefabsimulator.components.equipment.MaintenanceTriggeredEvent)
	 */
	@Override
	public void onMaintenanceTriggered(final MaintenanceTriggeredEvent event) {
		this.logger.trace("[{}] onMaintenanceTriggeredEvent", event.getComponent().getTime());
		this.logger.trace("Tool: {}  Item: {}", event.getComponent(), event.getFlowItem());
		final AbstractTool tool = (AbstractTool) event.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		this.logger.trace("Current State: {}", currentState);
		final AbstractToolState newState = currentState.onMaintenanceTriggered(tool, event.getMaintenance());

		updateStateAndStartNewStateWithEvent(tool, event, newState);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onOperatorBecomesAvailable(de.
	 * terministic.alternativefabsimulator.core.OperatorDemand)
	 */
	@Override
	public void onOperatorBecomesAvailable(final OperatorDemand operatorDemand) {
		final AbstractTool tool = operatorDemand.getTool();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		currentState.onOperatorBecomesAvailable(operatorDemand);
		// tool.setCurrentToolState(currentState.getSemiE10State(tool));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onOperatorFinishedProcessing(de.
	 * terministic.alternativefabsimulator.components.equipment.
	 * OperatorFinishedEvent)
	 */
	@Override
	public void onOperatorFinishedProcessing(final OperatorFinishedEvent operatorFinishedEvent) {
		final AbstractTool tool = (AbstractTool) operatorFinishedEvent.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		currentState.onOperatorFinished(operatorFinishedEvent);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onProcessFinished(de.terministic.
	 * alternativefabsimulator.components.equipment.ProcessFinishedEvent)
	 */
	@Override
	public void onProcessFinished(final ProcessFinishedEvent processFinishedEvent) {
		this.logger.trace("[{}] onProcessFinishedEvent", processFinishedEvent.getComponent().getTime());
		this.logger.trace("Tool: {}  Item: {}", processFinishedEvent.getComponent(),
				processFinishedEvent.getFlowItem());
		final AbstractTool tool = (AbstractTool) processFinishedEvent.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		this.logger.trace("Current State: {}", currentState);
		final AbstractFlowItem item = processFinishedEvent.getFlowItem();
		final AbstractToolState newState = currentState.onProcessFinished(tool, item);
		if (currentState != newState) {
			this.logger.trace("Current State: {} new State: {}", currentState, newState);
			updateStateAndStartNewStateWithItem(tool, item, newState);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onSetupFinishedEvent(de.terministic.
	 * alternativefabsimulator.components.equipment.SetupFinishedEvent)
	 */
	@Override
	public void onSetupFinishedEvent(final SetupFinishedEvent event) {
		this.logger.trace("[{}] onSetupFinishedEvent", event.getComponent().getTime());
		this.logger.trace("Tool: {}  Item: {}", event.getComponent(), event.getFlowItem());
		final AbstractTool tool = (AbstractTool) event.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);
		this.logger.trace("Current State: {}", currentState);
		final AbstractFlowItem item = event.getFlowItem();
		final AbstractToolState newState = currentState.onSetupFinished(tool, item);
		updateStateAndStartNewStateWithItem(tool, item, newState);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#onUnloadingFinished(de.terministic.
	 * alternativefabsimulator.components.equipment.UnloadingFinishedEvent)
	 */
	@Override
	public void onUnloadingFinished(final UnloadingFinishedEvent unloadingFinishedEvent) {
		final AbstractTool tool = (AbstractTool) unloadingFinishedEvent.getComponent();
		final AbstractToolState currentState = this.currentStateMap.get(tool);

		final AbstractToolState newState = currentState.onUnloadingFinished(unloadingFinishedEvent);
		updateStateAndStartNewStateWithItem(tool, unloadingFinishedEvent.getFlowItem(), newState);
	}

	public void pause(final AbstractTool tool) {
		this.currentStateMap.get(tool).getStateDetails().get(tool).pause();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#readyToProcess(de.terministic.
	 * alternativefabsimulator.components.equipment.AbstractTool,
	 * de.terministic.alternativefabsimulator.core.AbstractFlowItem)
	 */
	@Override
	public boolean readyToProcess(final AbstractTool tool, final AbstractFlowItem item) {
		return this.currentStateMap.get(tool).canProcess();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.terministic.alternativefabsimulator.components.equipment.
	 * toolstatemachine.IToolStateMachine#registerTool(de.terministic.
	 * alternativefabsimulator.components.equipment.AbstractTool)
	 */
	@Override
	public void registerTool(final AbstractTool tool) {
		this.currentStateMap.put(tool, this.initialState);
		this.queuedEvents.put(tool, new ArrayList<AbstractSimEvent>());
	}

	protected void resolveStoredEvent(final AbstractTool tool) {
		final ArrayList<AbstractSimEvent> sortedEvents = this.queuedEvents.get(tool);
		Collections.sort(sortedEvents, new EventComparator());
		this.queuedEvents.put(tool, sortedEvents);
		final AbstractSimEvent queuedEvent = this.queuedEvents.get(tool).remove(0);
		queuedEvent.resolveEvent();
	}

	public void resume(final AbstractTool tool) {
		this.currentStateMap.get(tool).getStateDetails().get(tool).resume();
	}

	public void setCurrentStateMap(final Map<AbstractTool, AbstractToolState> currentStateMap) {
		this.currentStateMap = currentStateMap;
	}

	public void setInitialState(final AbstractToolState initialState) {
		this.initialState = initialState;
	}

	public void setQueuedEvents(final Map<AbstractTool, ArrayList<AbstractSimEvent>> queuedEvents) {
		this.queuedEvents = queuedEvents;
	}

	private void updateStateAndResumeNewStateWithEvent(final AbstractTool tool, final AbstractSimEvent event,
			final AbstractToolState newState) {
		// this.logger.trace("[{}] updateStateAndResumeNewStateWithEvent {}",
		// tool.getTime(), newState);
		if (newState != null) {
			// this.logger.trace("newState is: {}", newState);
			this.currentStateMap.put(tool, newState);
			SemiE10EquipmentState newSemiState = newState.resumeState(tool, this.queuedEvents.get(tool));
			tool.setCurrentToolState(newSemiState);
			if (newState instanceof StandbyToolState) {
				if (!this.queuedEvents.get(tool).isEmpty()) {
					this.logger.trace("There are events left to do");
					resolveStoredEvent(tool);
				} else {
					this.logger.trace("There are no events left to do");
				}
			}
			if (this.currentStateMap.get(tool).needsEventForLater(event)) {
				this.queuedEvents.get(tool).add(event);
				this.logger.trace("Event stored for later use");
			}
		}

	}

	public void updateStateAndStartNewStateWithEvent(final AbstractTool tool, final AbstractSimEvent event,
			final AbstractToolState newState) {
		// this.logger.trace("[{}] new state is {}", tool.getTime(), newState);
		if (newState != null) {
			tool.setCurrentToolState(newState.getSemiE10State(tool));
			this.currentStateMap.put(tool, newState);
			if (this.queuedEvents.get(tool).isEmpty()) {
				newState.enterState(tool, event);
			} else {
				resolveStoredEvent(tool);
			}
		} else {
			if (this.currentStateMap.get(tool).needsEventForLater(event)) {
				this.queuedEvents.get(tool).add(event);
				this.logger.trace("Event stored for later use");
			}
		}
	}

	public void updateStateAndStartNewStateWithItem(final AbstractTool tool, final AbstractFlowItem item,
			final AbstractToolState newState) {
		if (tool.getName().equals("ToolGroup_3")) {
			this.logger.trace("[{}] new state is {}", tool.getTime(), newState);
		}
		if (newState != null) {
			if (this.queuedEvents.get(tool).isEmpty()) {
				this.logger.trace("There are no stored events");
				this.currentStateMap.put(tool, newState);
				newState.enterState(tool, item);
				tool.setCurrentToolState(newState.getSemiE10State(tool));
			} else {
				this.logger.trace("There are  stored events {}", this.queuedEvents.get(tool));
				tool.setCurrentToolState(newState.getSemiE10State(tool));
				this.currentStateMap.put(tool, newState);
				resolveStoredEvent(tool);
			}
		}
	}

}