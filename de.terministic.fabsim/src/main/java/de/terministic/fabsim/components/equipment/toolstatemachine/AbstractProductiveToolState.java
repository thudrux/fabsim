package de.terministic.fabsim.components.equipment.toolstatemachine;

import java.util.ArrayList;

import de.terministic.fabsim.components.ProcessStep;
import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.components.equipment.OperatorFinishedEvent;
import de.terministic.fabsim.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.components.equipment.toolstatemachine.ProcessStateDetails.State;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractOperatorGroup;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.OperatorDemand;

public abstract class AbstractProductiveToolState extends AbstractToolState {

	protected BreakdownToolState breakdownToolState;

	public AbstractProductiveToolState(final FabModel model) {
		super(model);
	}

	@Override
	public boolean canProcess() {
		return false;
	}

	private AbstractSimEvent createAndScheduleOperatorEvent(final ProcessStateDetails details) {
		return getFabModel().getSimulationEngine().getEventFactory().scheduleNewOperatorFinishedEvent(
				details.getRemainingOperatorTime(), details.getTool(), details.getItem());
	}

	protected abstract AbstractSimEvent createAndScheduleProcessEvent(ProcessStateDetails details);

	@Override
	public SemiE10EquipmentState enterState(final AbstractTool tool, final AbstractFlowItem item) {
		SemiE10EquipmentState resultState = null;
		if (tool.getName().equals("ToolGroup_2")) {
			this.logger.trace("{} is loaded into {}", item, tool);
		}
		final ProcessStateDetails details = new ProcessStateDetails(tool, null, null, null, null, null);
		if (tool.getName().equals("ToolGroup_2")) {
			logger.trace("[{}]Enter State with details: {}\n", tool.getTime(), details);
		}
		details.setItem(item);
		details.setRemainingProcessTime(getProcessTime(item));
		details.setRemainingOperatorTime(getOperatorTime(item, tool));
		tool.becomesUnavailable();

		final ProcessStep step = item.getCurrentStep();
		if (isOperatorNeeded(tool, step)) {
			if (tool.getName().equals("ToolGroup_2")) {
				this.logger.trace("Operator {} is needed for step", step.getOperatorGroup());
			}
			final AbstractOperatorGroup opGroup = step.getOperatorGroup();
			details.setOperatorInUse(opGroup);
			final OperatorDemand demand = new OperatorDemand(tool, item, details);
			details.setDemand(demand);
			if (step.getOperatorGroup().allocateOperator(demand)) {
				if (tool.getName().equals("ToolGroup_2")) {
					this.logger.trace("Operator was available and loading is starting");
				}
				final AbstractSimEvent opEvent = createAndScheduleOperatorEvent(details);
				details.setOperatorEvent(opEvent);
				final AbstractSimEvent toolEvent = createAndScheduleProcessEvent(details);
				details.setEndEvent(toolEvent);
				if (tool.getName().equals("ToolGroup_2")) {
					this.logger.trace("Setting ToolState to PR", step.getOperatorGroup());
				}
				resultState = SemiE10EquipmentState.PR;

			} else {
				if (tool.getName().equals("ToolGroup_2")) {
					this.logger.trace("Operator was not available, waiting for operator");
				}
				resultState = SemiE10EquipmentState.SB_NO_OPERATOR;
			}
		} else {
			final AbstractSimEvent event = createAndScheduleProcessEvent(details);
			details.setEndEvent(event);
			if (tool.getName().equals("ToolGroup_2")) {
				this.logger.trace("Setting ToolState to PR", step.getOperatorGroup());
			}
			resultState = SemiE10EquipmentState.PR;
		}
		if (tool.getName().equals("ToolGroup_2")) {
			this.logger.trace("new SEMIE10 state is {}", details.getToolState());
		}
		this.getStateDetails().put(tool, details);
		details.setToolState(resultState);
		return resultState;
	}

	public BreakdownToolState getBreakdownToolState() {
		return this.breakdownToolState;
	}

	protected abstract long getOperatorTime(AbstractFlowItem item, AbstractTool tool);

	protected abstract long getProcessTime(AbstractFlowItem item);

	@Override
	public SemiE10EquipmentState getSemiE10State(final AbstractTool tool) {
		this.logger.trace("Checking state for {}", tool);
		SemiE10EquipmentState result = null;
		if (getStateDetails().containsKey(tool)) {
			result = this.getStateDetails().get(tool).getToolState();
		} else {
			this.logger.error("{} is currently not in state processing", tool);
			throw new WrongToolStateException(tool.getName() + " is currently not in state processing");
		}
		this.logger.trace("current state of tool is {}", result);
		return result;
	}

	protected abstract boolean isOperatorNeeded(AbstractTool tool, ProcessStep step);

	@Override
	public boolean needsEventForLater(final ISimEvent event) {
		return event instanceof MaintenanceTriggeredEvent;
	}

	@Override
	public AbstractToolState onBreakdownTriggered(final AbstractTool tool, final IBreakdown breakdown) {
		// logger.trace("[{}] onBreakdownTriggert", tool.getTime());
		final ProcessStateDetails details = getStateDetails().get(tool);
		if (tool.getName().equals("ToolGroup_0") && tool.getTime() > (262716320L)) {
			logger.trace("Details: {}", details);
			logger.trace("new SEMIE10 state is {}", details.getToolState());
		}
		if ((details.getToolState().equals(SemiE10EquipmentState.PR)) && (details.getEndEvent() != null)) {
			details.setRemainingProcessTime(details.getEndEvent().getEventTime() - tool.getTime());
			if (details.getOperatorEvent() != null) {
				if (details.getOperatorInUse() != null) {// Operator has not yet
															// finished
					details.setRemainingOperatorTime(details.getOperatorEvent().getEventTime() - tool.getTime());
					details.getOperatorInUse().releaseOperator(details.getDemand());
					tool.unscheduleEvent(details.getOperatorEvent());
				} else {// Operator has finished working
					details.setRemainingOperatorTime(0);

				}
			}
			tool.unscheduleEvent(details.getEndEvent());
		} else {
			if (details.getOperatorInUse() != null) {
				details.getOperatorInUse().postponeDemand(details.getDemand());
			}
		}
		details.setState(State.PAUSED);
		return this.breakdownToolState;
	}

	@Override
	public AbstractToolState onMaintenanceTriggered(final AbstractTool tool, final IMaintenance maint) {
		return null;
	}

	@Override
	public void onOperatorBecomesAvailable(final OperatorDemand operatorDemand) {
		// if (operatorDemand.getTool().getName().equals("ToolGroup_3")) {
		// this.logger.trace("[{}] Operator is now available for {} on {}",
		// operatorDemand.getTool().getTime(),
		// operatorDemand.getItem(), operatorDemand.getTool());
		// }
		startTaskWithOperator(operatorDemand);
		// if (operatorDemand.getTool().getName().equals("ToolGroup_3")) {
		// this.logger.trace("[{}] Task started for {} on {}",
		// operatorDemand.getTool().getTime(),
		// operatorDemand.getItem(), operatorDemand.getTool());
		// }

	}

	@Override
	public void onOperatorFinished(final OperatorFinishedEvent operatorFinishedEvent) {
		// logger.trace("Start onOperatorFinished");
		final AbstractTool tool = (AbstractTool) operatorFinishedEvent.getComponent();
		final ProcessStateDetails details = getStateDetails().get(tool);
		details.getOperatorInUse().releaseOperator(details.getDemand());
		details.setOperatorInUse(null);
		details.setDemand(null);
		// logger.trace("End onOperatorFinished");
	}

	@Override
	public SemiE10EquipmentState resumeState(final AbstractTool tool, final ArrayList<AbstractSimEvent> storedEvents) {
		SemiE10EquipmentState result = SemiE10EquipmentState.PR;
		logger.trace("Resuming state {}", this);
		final ProcessStateDetails details = getStateDetails().get(tool);
		if (tool.getName().equals("ToolGroup_2") && tool.getTime() > (848073699L)) {
			logger.trace("resume State with details: {}\n", details);
		}
		details.setState(State.ONGOING);
		if (details.getRemainingOperatorTime() > 0L) {
			if (details.getOperatorInUse().allocateOperator(details.getDemand())) {
				if (tool.getName().equals("ToolGroup_2") && tool.getTime() > (848073699L)) {
					this.logger.trace("Operator was available and loading is starting");
				}
				if (details.getEndEvent() != null) {
					details.getOperatorEvent().setTime(tool.getTime() + details.getRemainingOperatorTime());
					tool.rescheduleEvent(details.getOperatorEvent());
					details.getEndEvent().setTime(tool.getTime() + details.getRemainingProcessTime());
					tool.rescheduleEvent(details.getEndEvent());
				} else {
					final AbstractSimEvent toolEvent = createAndScheduleProcessEvent(details);
					details.setEndEvent(toolEvent);
					final AbstractSimEvent opEvent = createAndScheduleOperatorEvent(details);
					details.setOperatorEvent(opEvent);
				}
			} else {
				if (tool.getName().equals("ToolGroup_2") && tool.getTime() > (848073699L)) {
					this.logger.trace("Operator was not available, waiting for operator");
				}
				result = SemiE10EquipmentState.SB_NO_OPERATOR;
			}

		} else {
			if (tool.getName().equals("ToolGroup_2") && tool.getTime() > (848073699L)) {
				logger.trace("RemainingProcessTime is {}", details.getRemainingProcessTime());
			}
			if (details.getEndEvent() != null) {
				details.getEndEvent().setTime(tool.getTime() + details.getRemainingProcessTime());
				tool.rescheduleEvent(details.getEndEvent());
			} else {
				final AbstractSimEvent toolEvent = createAndScheduleProcessEvent(details);
				details.setEndEvent(toolEvent);
			}
			if (tool.getName().equals("ToolGroup_2") && tool.getTime() > (848073699L)) {
				logger.trace("Rescheduling {}", details.getEndEvent());
			}
		}
		details.setToolState(result);
		return result;
	}

	public void setBreakdownToolState(final BreakdownToolState breakdownToolState) {
		this.breakdownToolState = breakdownToolState;
	}

	public void startTaskWithOperator(final OperatorDemand operatorDemand) {
		final ProcessStep step = operatorDemand.getItem().getCurrentStep();
		final AbstractTool tool = operatorDemand.getTool();
		if (step.getOperatorGroup().allocateOperator(operatorDemand)) {
			final ProcessStateDetails details = this.getStateDetails().get(tool);
			final AbstractSimEvent opEvent = createAndScheduleOperatorEvent(details);
			final AbstractSimEvent toolEvent = createAndScheduleProcessEvent(details);
			if (operatorDemand.getTool().getName().equals("ToolGroup_2")) {
				this.logger.trace("[{}] before setToolState", operatorDemand.getTool().getTime());
			}
			details.setToolState(SemiE10EquipmentState.PR);
			if (operatorDemand.getTool().getName().equals("ToolGroup_2")) {
				this.logger.trace("[{}] between setToolState", operatorDemand.getTool().getTime());
			}
			tool.setCurrentToolState(SemiE10EquipmentState.PR);
			if (operatorDemand.getTool().getName().equals("ToolGroup_2")) {
				this.logger.trace("[{}] after setToolState", operatorDemand.getTool().getTime());
			}
			details.setEndEvent(toolEvent);
			details.setOperatorEvent(opEvent);
		}
	}

}