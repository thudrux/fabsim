package de.terministic.fabsim.components.equipment.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.core.AbstractFlowItem;

public class BasicConditionBasedSetupStrategy extends AbstractSetupStrategy {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	public boolean checkConditions(final Collection<ISetupChangeCondition> conditions, final AbstractTool tool,
			final AbstractFlowItem item, final SetupState newState) {
		if (conditions != null) {
			for (final ISetupChangeCondition condition : conditions) {
				if (!condition.checkCondition(tool, item, newState))
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean filterForValidItem(final AbstractTool tool, final AbstractFlowItem item) {
		final SetupState newState = item.getCurrentStep().getSetupDetails();
		if (tool.getCurrentSetupState() == newState)
			return true;
		this.logger.trace("Starting to check preState conditions");
		boolean result = checkConditions(this.getPreStateConditionMap().get(tool.getCurrentSetupState()), tool, item,
				newState);
		this.logger.trace("Starting to check postState conditions");
		result = result && checkConditions(this.getPostStateConditionMap().get(newState), tool, item, newState);
		this.logger.trace("Starting to check global conditions");
		result = result && checkConditions(this.getGlobalConditions(), tool, item, newState);
		return result;
	}

	/*
	 * @Override public List<AbstractFlowItem>
	 * filterValidItems(AbstractToolGroup toolGroup, List<AbstractFlowItem>
	 * flowItems) { Map<SetupState, ArrayList<AbstractFlowItem>> stateItemMap =
	 * new LinkedHashMap<>(); for (AbstractFlowItem item:flowItems){ SetupState
	 * state = item.getCurrentStep().getSetupDetails(); if
	 * (!stateItemMap.containsKey(state)){ stateItemMap.put(state, new
	 * ArrayList<AbstractFlowItem>()); } stateItemMap.get(state).add(item); }
	 *
	 *
	 * return null;
	 *
	 * }
	 */
	@Override
	public ArrayList<AbstractFlowItem> filterValidItems(final AbstractTool tool,
			final ArrayList<AbstractFlowItem> possibleItems) {
		final Map<SetupState, Boolean> postStateValidity = new LinkedHashMap<>();
		final ArrayList<AbstractFlowItem> resultList = new ArrayList<>();
		for (final AbstractFlowItem item : possibleItems) {
			final SetupState postState = item.getCurrentStep().getSetupDetails();
			if (!postStateValidity.containsKey(postState)) {
				postStateValidity.put(postState, filterForValidItem(tool, item));
			}
			if (postStateValidity.get(postState)) {
				resultList.add(item);
			}
		}
		return resultList;
	}

}
