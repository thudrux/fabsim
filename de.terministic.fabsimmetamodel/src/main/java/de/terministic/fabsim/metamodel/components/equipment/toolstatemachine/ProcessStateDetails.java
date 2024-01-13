package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.AbstractOperatorGroup;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.OperatorDemand;

public class ProcessStateDetails {
	public enum State {
		ONGOING, PAUSED
	}

	private State state;
	private AbstractSimEvent endEvent;
	private long remainingProcessTime = -1L;
	private AbstractTool tool;
	private SemiE10EquipmentState toolState;
	private AbstractOperatorGroup operatorInUse;
	private AbstractSimEvent operatorEvent;
	private OperatorDemand demand;
	private AbstractFlowItem item;
	private long remainingOperatorTime;

	public ProcessStateDetails(final AbstractTool tool, final AbstractSimEvent event,
			final SemiE10EquipmentState toolState, final AbstractOperatorGroup opGroup, final AbstractSimEvent opEvent,
			final OperatorDemand demand) {
		this.endEvent = event;
		this.tool = tool;
		this.state = State.ONGOING;
		this.setOperatorInUse(opGroup);
		this.setOperatorEvent(opEvent);
		this.setDemand(demand);
	}

	public OperatorDemand getDemand() {
		return this.demand;
	}

	public AbstractSimEvent getEndEvent() {
		return this.endEvent;
	}

	public AbstractFlowItem getItem() {
		return this.item;
	}

	public AbstractSimEvent getOperatorEvent() {
		return this.operatorEvent;
	}

	public AbstractOperatorGroup getOperatorInUse() {
		return this.operatorInUse;
	}

	public long getRemainingOperatorTime() {
		return this.remainingOperatorTime;
	}

	public long getRemainingProcessTime() {
		return this.remainingProcessTime;
	}

	public State getState() {
		return this.state;
	}

	public AbstractTool getTool() {
		return this.tool;
	}

	public SemiE10EquipmentState getToolState() {
		return this.toolState;
	}

	public void pause() {
		this.state = State.PAUSED;
		this.remainingProcessTime = this.endEvent.getEventTime() - this.tool.getTime();
		this.tool.unscheduleEvent(this.endEvent);
	}

	public void resume() {
		this.state = State.ONGOING;
		this.endEvent.setTime(this.tool.getTime() + this.remainingProcessTime);
		this.tool.rescheduleEvent(this.endEvent);
	}

	public void setDemand(final OperatorDemand demand) {
		this.demand = demand;
	}

	public void setEndEvent(final AbstractSimEvent endEvent) {
		this.endEvent = endEvent;
	}

	public void setItem(final AbstractFlowItem item) {
		this.item = item;
	}

	public void setOperatorEvent(final AbstractSimEvent operatorEvent) {
		this.operatorEvent = operatorEvent;
	}

	public void setOperatorInUse(final AbstractOperatorGroup operatorInUse) {
		this.operatorInUse = operatorInUse;
	}

	public void setRemainingOperatorTime(final long remainingOperatorTime) {
		this.remainingOperatorTime = remainingOperatorTime;
	}

	public void setRemainingProcessTime(final long remainingTime) {
		this.remainingProcessTime = remainingTime;
	}

	public void setState(final State state) {
		this.state = state;
	}

	public void setTool(final AbstractTool tool) {
		this.tool = tool;
	}

	public void setToolState(final SemiE10EquipmentState toolState) {
		this.toolState = toolState;
	}

	public String toString() {
		String result = "\nState: " + state.name();
		result += "\nremainingOpTime: " + this.remainingOperatorTime;
		result += "\nremainingProcessingTime: " + this.remainingProcessTime;
		result += "\noperatorInUse: " + this.operatorInUse;
		result += "\nEndEvent: " + this.endEvent;

		return result;
	}
}
