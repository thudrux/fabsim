/*
 *
 * @author    Falk Pappert
 */
package de.terministic.fabsim.components.equipment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import de.terministic.fabsim.components.FlowItemArrivalEvent;
import de.terministic.fabsim.components.ToolGroupController;
import de.terministic.fabsim.components.equipment.dedication.Dedication;
import de.terministic.fabsim.components.equipment.dedication.DedicationDetails;
import de.terministic.fabsim.components.equipment.toolstatemachine.AbstractToolStateMachine;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.OperatorDemand;

public abstract class AbstractTool extends AbstractResource {

	protected SetupState currentSetupState;

	protected HashSet<Dedication> dedications = new HashSet<>();

	public HashSet<Dedication> getDedications() {
		return dedications;
	}

	public void setDedications(HashSet<Dedication> dedications) {
		this.dedications = dedications;
	}

	private ToolGroupController tgController;

	protected AbstractFlowItem item;
	protected AbstractToolStateMachine toolStateMachine;

	protected int opLoadPercentage = 0;

	protected int opProcessingPercentage = 0;
	protected int opUnloadPercentage = 0;

	SetupState initialSetupState;

	private Map<SetupState, LinkedHashMap<SetupState, Long>> setupTransitions = new LinkedHashMap<>();

	public AbstractTool(FabModel model, final String name, final AbstractResourceGroup toolGroup,
			final AbstractToolStateMachine stateMachine) {
		super(model, name);
		this.parent = toolGroup;
		this.toolStateMachine = stateMachine;
		this.toolStateMachine.registerTool(this);
	}

	public abstract void becomesAvailable();

	public abstract void becomesUnavailable();

	@Override
	public boolean canProcessItem(final AbstractFlowItem item) {
		this.logger.trace("[{}]canProcess: {} on {}({})", getTime(), item, this,
				this.toolStateMachine.getCurrentSemiE10StateOfTool(this));
		boolean result = this.toolStateMachine.readyToProcess(this, item);
		this.logger.trace("result is {}", result);
		return result;
	}

	public void finishProcessingOfFlowItem(final AbstractFlowItem flowItem) {
		final FinishedHandlingEvent event = new FinishedHandlingEvent(getModel(), getTime(), this, flowItem);
		getSimulationEngine().getEventList().scheduleEvent(event);
		((AbstractToolGroup) this.parent).takeAfterProcessing(flowItem, this);
	}

	public SetupState getCurrentSetupState() {
		return this.currentSetupState;
	}

	public SemiE10EquipmentState getCurrentToolState() {
		return this.toolStateMachine.getCurrentSemiE10StateOfTool(this);
	}

	public SetupState getInitialSetupState() {
		return this.initialSetupState;
	}

	public int getOpLoadPercentage() {
		return this.opLoadPercentage;
	}

	public int getOpProcessingPercentage() {
		return this.opProcessingPercentage;
	}

	public int getOpUnloadPercentage() {
		return this.opUnloadPercentage;
	}

	@Override
	public AbstractResourceGroup getParent() {
		return this.parent;
	}

	public Map<SetupState, LinkedHashMap<SetupState, Long>> getSetupTransitions() {
		return this.setupTransitions;
	}

	public ToolGroupController getTGController() {
		return this.tgController;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	public boolean needsSetupFor(final AbstractFlowItem item) {
		final SetupState desiredState = item.getCurrentStep().getSetupDetails();
		if ((desiredState == null) || desiredState.equals(this.currentSetupState))
			return false;
		if (this.currentSetupState == null) {
			this.currentSetupState = desiredState;
			return false;
		}
		return true;
	}

	@Override
	public void onBreakdownFinished(final BreakdownFinishedEvent event) {
		this.toolStateMachine.onBreakdownFinished(event);
	}

	@Override
	public void onBreakdownTriggered(final BreakdownTriggeredEvent event) {
		this.toolStateMachine.onBreakdownTriggered(event);

	}

	@Override
	public void onLoadingFinished(final LoadingFinishedEvent loadingFinishedEvent) {
		this.toolStateMachine.onLoadingFinished(loadingFinishedEvent);
	}

	@Override
	public void onMaintenanceFinished(final MaintenanceFinishedEvent event) {
		this.toolStateMachine.onMaintenanceFinished(event);
	}

	@Override
	public void onMaintenanceTriggered(final MaintenanceTriggeredEvent maintenanceTriggeredEvent) {
		this.toolStateMachine.onMaintenanceTriggered(maintenanceTriggeredEvent);

	}

	@Override
	public void onOperatorBecomesAvailable(final OperatorDemand operatorDemand) {
		this.toolStateMachine.onOperatorBecomesAvailable(operatorDemand);

	}

	@Override
	public void onOperatorFinishedProcessing(final OperatorFinishedEvent operatorFinishedEvent) {
		this.toolStateMachine.onOperatorFinishedProcessing(operatorFinishedEvent);
	}

	@Override
	public void onProcessFinished(final ProcessFinishedEvent processFinishedEvent) {
		this.toolStateMachine.onProcessFinished(processFinishedEvent);

	}

	@Override
	public void onSetupFinished(final SetupFinishedEvent event) {
		this.toolStateMachine.onSetupFinishedEvent(event);
	}

	@Override
	public void onUnloadingFinished(final UnloadingFinishedEvent unloadingFinishedEvent) {
		this.toolStateMachine.onUnloadingFinished(unloadingFinishedEvent);
	}

	public void rescheduleEvent(final AbstractSimEvent event) {
		getSimulationEngine().getEventList().scheduleEvent(event);
	}

	public void setCurrentSetupState(final SetupState currentSetupState) {
		this.currentSetupState = currentSetupState;
	}

	public void setCurrentToolState(final SemiE10EquipmentState currentToolState) {
		this.logger.trace("Starting setCurrentToolState({})", currentToolState);
		if (currentToolState != null) {
			final StateChangeEvent event = new StateChangeEvent(getModel(), getSimulationEngine().getTime(), this,
					currentToolState);
			event.setSetupState(this.currentSetupState);
			getSimulationEngine().getEventList().scheduleEvent(event);
		}
	}

	public void setInitialSetupState(final SetupState initialSetupState) {
		this.currentSetupState = initialSetupState;
		this.initialSetupState = initialSetupState;

	}

	public void setOpLoadPercentage(final int opLoadPercentage) {
		this.opLoadPercentage = opLoadPercentage;
	}

	public void setOpProcessingPercentage(final int opProcessingPercentage) {
		this.opProcessingPercentage = opProcessingPercentage;
	}

	public void setOpUnloadPercentage(final int opUnloadPercentage) {
		this.opUnloadPercentage = opUnloadPercentage;
	}

	public void setSetupTransitions(final Map<SetupState, LinkedHashMap<SetupState, Long>> setupTransitions) {
		this.setupTransitions = setupTransitions;
	}

	public void setTGController(final ToolGroupController tgController) {
		this.tgController = tgController;
	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		getSimulationEngine().getEventList()
				.scheduleEvent(new StartHandlingEvent(getModel(), getTime(), this, event.getFlowItem()));

	}

	public void unscheduleEvent(final ISimEvent event) {
		getSimulationEngine().getEventList().unscheduleEvent(event);
	}

	public ArrayList<AbstractFlowItem> dedicationFilter(ArrayList<AbstractFlowItem> possibleItems) {
		ArrayList<AbstractFlowItem> result = new ArrayList<>();
		for (AbstractFlowItem item : possibleItems) {
			DedicationDetails details = ((DedicationDetails) item.getCurrentStep().getDetails());
			if (this.dedications.contains(details.getNecessaryQualification())) {
				result.add(item);
			}
		}
		return result;
	}

}