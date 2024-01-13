package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.ISimEvent;

public class MaintenanceToolState extends AbstractToolState {
	private StandbyToolState standbyToolState;

	private BreakdownToolState breakdownToolState;

	public MaintenanceToolState(final FabModel model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canProcess() {
		return false;
	}

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final IMaintenance maintenance) {
		// logger.trace("enter state {} with {}", tool, maintenance);
		tool.becomesUnavailable();
		final AbstractSimEvent event = getFabModel().getEventFactory()
				.scheduleNewMaintenanceFinishedEvent(maintenance.getDuration(), tool, maintenance);
		final ProcessStateDetails details = new ProcessStateDetails(tool, event, SemiE10EquipmentState.SD_MAINT, null,
				null, null);
		this.getStateDetails().put(tool, details);
		return SemiE10EquipmentState.SD_MAINT;
	}

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final ISimEvent event) {
		// logger.trace("enter state {} with {}", tool, event);
		tool.becomesUnavailable();
		if (event instanceof MaintenanceTriggeredEvent) {
			tool.becomesUnavailable();
			this.enterState(tool, ((MaintenanceTriggeredEvent) event).getMaintenance());
		} else {
			super.enterState(tool, event);
		}
		return SemiE10EquipmentState.SD_MAINT;
	}

	@Override
	public SemiE10EquipmentState getSemiE10State(final AbstractTool tool) {
		return SemiE10EquipmentState.SD_MAINT;
	}

	@Override
	public boolean needsEventForLater(final ISimEvent event) {
		return event instanceof MaintenanceTriggeredEvent;
	}

	@Override
	public AbstractToolState onBreakdownTriggered(final AbstractTool tool, final IBreakdown breakdown) {
		logger.trace("Tool: {}", tool);
		logger.trace("State Details: {}", getStateDetails().get(tool));
		getStateDetails().get(tool).pause();
		return this.breakdownToolState;
	}

	@Override
	public AbstractToolState onMaintenanceFinished(final AbstractTool tool, final IMaintenance maintenance) {
		this.logger.trace("onMaintFinished was called");
		maintenance.maintenanceFinished(tool);
		getStateDetails().remove(tool);
		return this.standbyToolState;
	}

	@Override
	public AbstractToolState onMaintenanceTriggered(final AbstractTool tool, final IMaintenance maint) {
		return null;
	}

	@Override
	public SemiE10EquipmentState resumeState(final AbstractTool tool, final ArrayList<AbstractSimEvent> storedEvents) {
		getStateDetails().get(tool).resume();
		return SemiE10EquipmentState.SD_MAINT;
	}

	public void setBreakdownToolState(final BreakdownToolState ud) {
		this.breakdownToolState = ud;
	}

	public void setStandbyToolState(final StandbyToolState sb) {
		this.standbyToolState = sb;
	}

}
