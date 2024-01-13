package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;

public class ProcessingToolState extends AbstractProductiveToolState {

	private StandbyToolState standbyToolState;

	private UnloadingToolState unloadingToolState;

	public ProcessingToolState(final FabModel model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AbstractSimEvent createAndScheduleProcessEvent(final ProcessStateDetails details) {
		return getFabModel().getSimulationEngine().getEventFactory().scheduleNewProcessFinishedEvent(
				details.getRemainingProcessTime(), details.getTool(), details.getItem());
	}

	@Override
	protected long getOperatorTime(final AbstractFlowItem item, final AbstractTool tool) {
		return item.getCurrentStep().getDuration(item) * tool.getOpProcessingPercentage() / 100;
	}

	@Override
	protected long getProcessTime(final AbstractFlowItem item) {
		return item.getCurrentStep().getDuration(item);
	}

	public UnloadingToolState getUnloadingToolState() {
		return this.unloadingToolState;
	}

	@Override
	protected boolean isOperatorNeeded(final AbstractTool tool, final ProcessStep step) {
		return (step.getOperatorGroup() != null) && tool.getOpProcessingPercentage() > 0;
	}

	@Override
	public AbstractToolState onProcessFinished(final AbstractTool tool, final AbstractFlowItem item) {
		final ProcessStep step = item.getCurrentStep();
		getStateDetails().remove(tool);
		if (step.getUnloadTime() > 0L)
			return this.unloadingToolState;
		else {
			this.logger.trace("Processing finished and no unload required.");
			tool.finishProcessingOfFlowItem(item);// hand back item to ToolGroup
			return this.standbyToolState;
		}
	}

	public void setStandbyToolState(final StandbyToolState sb) {
		this.standbyToolState = sb;

	}

	public void setUnloadingToolState(final UnloadingToolState unloadingToolState) {
		this.unloadingToolState = unloadingToolState;
	}

}
