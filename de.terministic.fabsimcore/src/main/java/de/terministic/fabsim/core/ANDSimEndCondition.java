package de.terministic.fabsim.core;

public class ANDSimEndCondition implements ISimEndCondition {

	private ISimEndCondition condition1;
	private ISimEndCondition condition2;
	public ANDSimEndCondition(ISimEndCondition condition1, ISimEndCondition condition2) {
		this.condition1=condition1;
		this.condition2=condition2;
	}
	@Override
	public boolean isConditionFulfilled(ISimEvent event) {
		return condition1.isConditionFulfilled(event)||condition2.isConditionFulfilled(event);
	}

}
