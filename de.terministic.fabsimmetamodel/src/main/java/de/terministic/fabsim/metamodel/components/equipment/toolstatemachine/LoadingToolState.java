package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.LoadingFinishedEvent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;

public class LoadingToolState extends AbstractProductiveToolState {
	private ProcessingToolState processingToolState;

	public LoadingToolState(final FabModel model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AbstractSimEvent createAndScheduleProcessEvent(final ProcessStateDetails details) {
		return getFabModel().getEventFactory().scheduleNewLoadingFinishedEvent(
				details.getRemainingProcessTime(), details.getTool(), details.getItem());
	}

	@Override
	public BreakdownToolState getBreakdownToolState() {
		return this.breakdownToolState;
	}

	@Override
	protected long getOperatorTime(final AbstractFlowItem item, final AbstractTool tool) {
		return item.getCurrentStep().getLoadTime() * tool.getOpLoadPercentage() / 100;
	}

	public ProcessingToolState getProcessingToolState() {
		return this.processingToolState;
	}

	@Override
	protected long getProcessTime(final AbstractFlowItem item) {
		return item.getCurrentStep().getLoadTime();
	}

	@Override
	protected boolean isOperatorNeeded(final AbstractTool tool, final ProcessStep step) {
		return (step.getOperatorGroup() != null) && tool.getOpLoadPercentage() > 0;
	}

	@Override
	public AbstractToolState onLoadingFinished(final LoadingFinishedEvent loadingFinishedEvent) {
		this.logger.trace("Loading finished");
		getStateDetails().remove(loadingFinishedEvent.getComponent());
		return this.processingToolState;
	}

	@Override
	public void setBreakdownToolState(final BreakdownToolState breakdownToolState) {
		this.breakdownToolState = breakdownToolState;
	}

	public void setProcessingToolState(final ProcessingToolState processingToolState) {
		this.processingToolState = processingToolState;
	}

}
