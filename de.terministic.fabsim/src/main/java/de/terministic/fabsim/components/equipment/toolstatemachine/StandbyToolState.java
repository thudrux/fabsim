package de.terministic.fabsim.components.equipment.toolstatemachine;

import java.util.ArrayList;

import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.BreakdownFinishedEvent;
import de.terministic.fabsim.components.equipment.MaintenanceFinishedEvent;
import de.terministic.fabsim.components.equipment.ProcessFinishedEvent;
import de.terministic.fabsim.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.components.equipment.UnloadingFinishedEvent;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.ISimEvent;

public class StandbyToolState extends AbstractToolState {

	private BreakdownToolState breakdownToolState;

	private MaintenanceToolState maintenanceToolState;
	private ProcessingToolState processingToolState;
	private SetupToolState setupToolState;
	private AbstractProductiveToolState loadingToolState;

	public StandbyToolState(final FabModel model) {
		super(model);
	}

	@Override
	public boolean canProcess() {
		return true;
	}

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final AbstractFlowItem item) {
		tool.becomesAvailable();
		return SemiE10EquipmentState.SB_NO_MATERIAL;
	}

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final ISimEvent event) {
		this.logger.trace("{} is entering StandbyState", tool);
		if ((event instanceof ProcessFinishedEvent) || (event instanceof UnloadingFinishedEvent)
				|| (event instanceof MaintenanceFinishedEvent) || (event instanceof BreakdownFinishedEvent)) {
			tool.becomesAvailable();
		} else {
			super.enterState(tool, event);
		}
		return SemiE10EquipmentState.SB_NO_MATERIAL;
	}

	public BreakdownToolState getBreakdownToolState() {
		return this.breakdownToolState;
	}

	public AbstractProductiveToolState getLoadingToolState() {
		return this.loadingToolState;
	}

	public MaintenanceToolState getMaintenanceToolState() {
		return this.maintenanceToolState;
	}

	public ProcessingToolState getProcessingToolState() {
		return this.processingToolState;
	}

	@Override
	public SemiE10EquipmentState getSemiE10State(final AbstractTool tool) {
		return SemiE10EquipmentState.SB_NO_MATERIAL;
	}

	protected SetupToolState getSetupToolState() {
		return this.setupToolState;
	}

	@Override
	public AbstractToolState onBreakdownTriggered(final AbstractTool tool, final IBreakdown breakdown) {
		return this.breakdownToolState;
	}

	@Override
	public AbstractToolState onFlowItemArrival(final AbstractTool tool, final AbstractFlowItem item) {
		this.logger.trace("onFlowItemArrival called. Tool: {} Item: {}", tool, item);
		if (tool.needsSetupFor(item))
			return this.setupToolState;
		else {
			if (item.getCurrentStep().getLoadTime() > 0L)
				return this.loadingToolState;
			else
				return this.processingToolState;
		}
	}

	@Override
	public AbstractToolState onMaintenanceTriggered(final AbstractTool tool, final IMaintenance maint) {
		return this.maintenanceToolState;
	}

	@Override
	public SemiE10EquipmentState resumeState(final AbstractTool tool, final ArrayList<AbstractSimEvent> storedEvents) {
		this.logger.trace("{} is resuming Standby", tool);
		if (storedEvents.isEmpty()) {
			tool.becomesAvailable();
		}
		return SemiE10EquipmentState.SB_NO_MATERIAL;
	}

	public void setBreakdownToolState(final BreakdownToolState breakdownToolState) {
		this.breakdownToolState = breakdownToolState;
	}

	public void setLoadingToolState(final AbstractProductiveToolState loadingToolState) {
		this.loadingToolState = loadingToolState;
	}

	public void setMaintenanceToolState(final MaintenanceToolState maintenanceToolState) {
		this.maintenanceToolState = maintenanceToolState;
	}

	public void setProcessingToolState(final ProcessingToolState processingToolState) {
		this.processingToolState = processingToolState;
	}

	protected void setSetupState(final SetupToolState setupToolState) {
		this.setupToolState = setupToolState;
	}

	public void setSetupToolState(final SetupToolState setupToolState) {
		this.setupToolState = setupToolState;
	}
}
