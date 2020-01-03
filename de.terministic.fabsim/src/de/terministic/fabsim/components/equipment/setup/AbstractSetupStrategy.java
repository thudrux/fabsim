package de.terministic.fabsim.components.equipment.setup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.core.AbstractFlowItem;

public abstract class AbstractSetupStrategy {
	private Map<SetupState, ArrayList<ISetupChangeCondition>> preStateConditionMap = new LinkedHashMap<>();
	private Map<SetupState, ArrayList<ISetupChangeCondition>> postStateConditionMap = new LinkedHashMap<>();
	private List<ISetupChangeCondition> globalConditions = new ArrayList<>();

	public void addGlobalCondition(final ISetupChangeCondition condition) {
		this.globalConditions.add(condition);
	}

	public void addPostCondition(final ISetupChangeCondition condition) {
		final SetupState state = condition.getState();
		if (!this.postStateConditionMap.containsKey(state)) {
			this.postStateConditionMap.put(condition.getState(), new ArrayList<ISetupChangeCondition>());
		}
		this.postStateConditionMap.get(state).add(condition);
	}

	public void addPreCondition(final ISetupChangeCondition condition) {
		final SetupState state = condition.getState();
		if (!this.preStateConditionMap.containsKey(state)) {
			this.preStateConditionMap.put(condition.getState(), new ArrayList<ISetupChangeCondition>());
		}
		this.preStateConditionMap.get(state).add(condition);
	}

	public abstract boolean filterForValidItem(AbstractTool tool, AbstractFlowItem item);

	public abstract ArrayList<AbstractFlowItem> filterValidItems(AbstractTool tool,
			ArrayList<AbstractFlowItem> possibleItems);

	public List<ISetupChangeCondition> getGlobalConditions() {
		return this.globalConditions;
	}

	public Map<SetupState, ArrayList<ISetupChangeCondition>> getPostStateConditionMap() {
		return this.postStateConditionMap;
	}

	public Map<SetupState, ArrayList<ISetupChangeCondition>> getPreStateConditionMap() {
		return this.preStateConditionMap;
	}

	public void initialize(final AbstractToolGroup toolGroup) {
		for (final List<ISetupChangeCondition> list : this.preStateConditionMap.values()) {
			for (final ISetupChangeCondition condition : list) {
				condition.initialize(toolGroup);
			}
		}
		for (final List<ISetupChangeCondition> list : this.postStateConditionMap.values()) {
			for (final ISetupChangeCondition condition : list) {
				condition.initialize(toolGroup);
			}
		}
		for (final ISetupChangeCondition condition : this.globalConditions) {
			condition.initialize(toolGroup);
		}
	}

	public void setGlobalConditions(final List<ISetupChangeCondition> globalConditions) {
		this.globalConditions = globalConditions;
	}

	public void setPostStateConditionMap(
			final Map<SetupState, ArrayList<ISetupChangeCondition>> postStateConditionMap) {
		this.postStateConditionMap = postStateConditionMap;
	}

	public void setPreStateConditionMap(final Map<SetupState, ArrayList<ISetupChangeCondition>> preStateConditionMap) {
		this.preStateConditionMap = preStateConditionMap;
	}

}
