package de.terministic.fabsim.components.equipment.toolstatemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.BreakdownTriggeredEvent;
import de.terministic.fabsim.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.ISimEvent;

public class BreakdownToolState extends AbstractToolState {
	private final HashMap<AbstractTool, AbstractToolState> previousStateMap = new LinkedHashMap<>();

	private final HashMap<AbstractTool, ArrayList<IBreakdown>> breakdownStack = new LinkedHashMap<>();// TODO:
	// rethink
	// what
	// to
	// store
	// in
	// this
	// maps

	public IBreakdown getNextBreakdownInStack(AbstractTool tool) {
		return breakdownStack.get(tool).remove(0);
	}

	public BreakdownToolState(final FabModel model) {
		super(model);

		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canProcess(final AbstractFlowItem item) {
		return false;
	}

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final IBreakdown breakdown) {
		tool.becomesUnavailable();
		getFabModel().getSimulationEngine().getEventFactory().scheduleNewBreakdownFinishedEvent(breakdown.getDuration(),
				tool, breakdown);
		return SemiE10EquipmentState.UD;
	}

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final ISimEvent event) {
		// logger.debug("enterState({}, {}", tool, event);
		tool.becomesUnavailable();
		if (event instanceof BreakdownTriggeredEvent) {
			tool.becomesUnavailable();
			this.enterState(tool, ((BreakdownTriggeredEvent) event).getBreakdown());
		} else {
			super.enterState(tool, event);
		}
		return SemiE10EquipmentState.UD;
	}

	@Override
	public SemiE10EquipmentState getSemiE10State(final AbstractTool tool) {
		return SemiE10EquipmentState.UD;
	}

	@Override
	public boolean needsEventForLater(final ISimEvent event) {
		return (event instanceof MaintenanceTriggeredEvent);
	}

	@Override
	public AbstractToolState onBreakdownFinished(final AbstractTool tool, final IBreakdown breakdown) {
		this.logger.trace("onBreakdownFinished was called");
		breakdown.breakdownFinished(tool);
		if (breakdownStack.containsKey(tool) && breakdownStack.get(tool).size() > 0) {
			return this;
		} else
			return this.previousStateMap.remove(tool);
	}

	@Override
	public AbstractToolState onBreakdownTriggered(final AbstractTool tool, final IBreakdown down) {
		if (this.breakdownStack.get(tool) == null) {
			this.breakdownStack.put(tool, new ArrayList<IBreakdown>());
		}
		this.breakdownStack.get(tool).add(down);
		return null;
	}

	@Override
	public AbstractToolState onMaintenanceTriggered(final AbstractTool tool, final IMaintenance maint) {
		return null;
	}

	public void setMaintenanceToolState(final MaintenanceToolState maint) {
	}

	public void setPreviousState(final AbstractTool tool, final AbstractToolState previousState) {
		this.previousStateMap.put(tool, previousState);
		this.logger.trace("{} is going down, previous state is {}", tool, previousState);
	}

	public void setStandbyToolState(final StandbyToolState sb) {
	}

}
