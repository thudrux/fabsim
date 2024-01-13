package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class SetupToolState extends AbstractToolState {

	private ProcessingToolState processingToolState;

	private BreakdownToolState breakdownToolState;
	private AbstractProductiveToolState loadingToolState;

	public SetupToolState(final FabModel model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canProcess() {
		return false;
	}

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final AbstractFlowItem item) {
		this.logger.trace("Starting enterState with tool {} and item {}", tool, item);
		tool.becomesUnavailable();
		final SetupState followingState = item.getCurrentStep().getSetupDetails();
		this.logger.trace("followingState is {}", followingState);
		final SetupState currentState = tool.getCurrentSetupState();
		this.logger.trace("currentState is {}", currentState);
		final long duration = tool.getSetupTransitions().get(currentState).get(followingState);
		final AbstractSimEvent event = getFabModel().getEventFactory()
				.scheduleNewSetupFinishedEvent(duration, tool, item);
		final ProcessStateDetails details = new ProcessStateDetails(tool, event, SemiE10EquipmentState.SD_SETUP, null,
				null, null);
		this.getStateDetails().put(tool, details);
		return SemiE10EquipmentState.SD_SETUP;
	}

	public AbstractProductiveToolState getLoadingToolState() {
		return this.loadingToolState;
	}

	public ProcessingToolState getProcessingState() {
		return this.processingToolState;
	}

	@Override
	public SemiE10EquipmentState getSemiE10State(final AbstractTool tool) {
		return SemiE10EquipmentState.SD_SETUP;
	}

	@Override
	public AbstractToolState onBreakdownTriggered(final AbstractTool tool, final IBreakdown breakdown) {
		getStateDetails().get(tool).pause();
		return this.breakdownToolState;
	}

	@Override
	public AbstractToolState onSetupFinished(final AbstractTool tool, final AbstractFlowItem item) {
		tool.setCurrentSetupState(item.getCurrentStep().getSetupDetails());
		getStateDetails().remove(tool);
		if (item.getCurrentStep().getLoadTime() > 0L)
			return this.loadingToolState;
		else
			return this.processingToolState;

	}

	@Override
	public SemiE10EquipmentState resumeState(final AbstractTool tool, final ArrayList<AbstractSimEvent> storedEvents) {
		getStateDetails().get(tool).resume();
		return SemiE10EquipmentState.SD_SETUP;
	}

	public void setBreakdownToolState(final BreakdownToolState ud) {
		this.breakdownToolState = ud;

	}

	public void setLoadingToolState(final AbstractProductiveToolState loadingToolState) {
		this.loadingToolState = loadingToolState;
	}

	public void setMaintenanceToolState(final MaintenanceToolState maint) {
	}

	public void setProcessingToolState(final ProcessingToolState pr) {
		this.processingToolState = pr;
	}

}
