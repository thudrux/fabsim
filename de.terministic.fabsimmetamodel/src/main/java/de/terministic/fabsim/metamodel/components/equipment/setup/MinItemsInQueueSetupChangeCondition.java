package de.terministic.fabsim.metamodel.components.equipment.setup;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.NotYetImplementedException;

public class MinItemsInQueueSetupChangeCondition implements ISetupChangeCondition {
	private final SetupState state;
	private final int count;
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getSimpleName());

	public MinItemsInQueueSetupChangeCondition(final SetupState state, final int count) {
		this.state = state;
		this.count = count;
	}

	@Override
	public boolean checkCondition(final AbstractTool tool, final AbstractFlowItem item, final SetupState newState) {
		this.logger.trace("Starting to check items in queue for {}", newState);
		if (this.state != newState) {
			this.logger.warn("{} should not have been evaluated for item {}", this, item);
			return true;
		}
		final ToolGroup toolGroup = (ToolGroup) tool.getParent();
		int setupCount = 0;
		for (final String key : toolGroup.getTGController().getBatchIdsForToolGroup(toolGroup)) {
			final Collection<AbstractFlowItem> list = toolGroup.getTGController().getQueueForBatchId(toolGroup, key);
			this.logger.trace("ItemMapKey: {} has {} items {}", key, list.size(), list);
			for (final AbstractFlowItem queuedItem : list) {
				this.logger.trace("checking {}", queuedItem);
				if (newState.equals(queuedItem.getCurrentStep().getSetupDetails())) {
					setupCount++;
					this.logger.trace("{} needs setupState {}", queuedItem, newState);
					if (setupCount >= this.count) {
						this.logger.trace("found {} items which need {}", setupCount, newState);
						return true;
					}
				}
			}
		}
		this.logger.trace("Could only find {} items which need {}", setupCount, newState);
		return false;
	}

	@Override
	public boolean checkCondition(final AbstractToolGroup toolGroup, final List<AbstractFlowItem> flowItems,
			final AbstractTool tool, final SetupState newState) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	public int getCount() {
		return this.count;
	}

	@Override
	public SetupState getState() {
		return this.state;
	}

	@Override
	public void initialize(final AbstractToolGroup toolGroup) {
		// Do nothing
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "_" + this.state + "_" + this.count;
	}
}
