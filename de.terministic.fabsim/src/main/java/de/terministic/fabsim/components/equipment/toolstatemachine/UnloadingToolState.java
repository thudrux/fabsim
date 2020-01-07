package de.terministic.fabsim.components.equipment.toolstatemachine;

import de.terministic.fabsim.components.ProcessStep;
import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.UnloadingFinishedEvent;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class UnloadingToolState extends AbstractProductiveToolState {
	private StandbyToolState standbyToolState;

	public UnloadingToolState(final FabModel model) {
		super(model);
	}

	@Override
	protected AbstractSimEvent createAndScheduleProcessEvent(final ProcessStateDetails details) {
		return getFabModel().getSimulationEngine().getEventFactory().scheduleNewUnloadingFinishedEvent(
				details.getRemainingProcessTime(), details.getTool(), details.getItem());
	}

	@Override
	protected long getOperatorTime(final AbstractFlowItem item, final AbstractTool tool) {
		return item.getCurrentStep().getUnloadTime() * tool.getOpUnloadPercentage() / 100;
	}

	@Override
	protected long getProcessTime(final AbstractFlowItem item) {
		return item.getCurrentStep().getUnloadTime();
	}

	public StandbyToolState getStandbyToolState() {
		return this.standbyToolState;
	}

	@Override
	protected boolean isOperatorNeeded(final AbstractTool tool, final ProcessStep step) {
		return (step.getOperatorGroup() != null) && tool.getOpUnloadPercentage() > 0;
	}

	@Override
	public AbstractToolState onUnloadingFinished(final UnloadingFinishedEvent unloadingFinishedEvent) {
		getStateDetails().remove(unloadingFinishedEvent.getComponent());
		((AbstractTool) unloadingFinishedEvent.getComponent())
				.finishProcessingOfFlowItem(unloadingFinishedEvent.getFlowItem());
		return this.standbyToolState;
	}

	public void setStandbyToolState(final StandbyToolState standbyToolState) {
		this.standbyToolState = standbyToolState;
	}

}
