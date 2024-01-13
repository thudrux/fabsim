package de.terministic.fabsim.metamodel.components.equipment.setup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;

public class MinProducedItemsSinceChangeSetupChangeCondition implements ISetupChangeCondition {
	private final Map<AbstractTool, Integer> toolMap = new LinkedHashMap<>();
	private final SetupState state;
	private final int count;

	public MinProducedItemsSinceChangeSetupChangeCondition(final SetupState state, final int count) {
		this.state = state;
		this.count = count;
	}

	@Override
	public boolean checkCondition(final AbstractTool tool, final AbstractFlowItem item, final SetupState newState) {
		return this.toolMap.get(tool) >= 0;
	}

	@Override
	public boolean checkCondition(final AbstractToolGroup toolGroup, final List<AbstractFlowItem> flowItems,
			final AbstractTool tool, final SetupState newState) {
		throw new NotYetImplementedException();
	}

	public long getCount() {
		return this.count;
	}

	@Override
	public SetupState getState() {
		return this.state;
	}

	@Override
	public void initialize(final AbstractToolGroup toolGroup) {
		for (final AbstractTool tool : toolGroup.getTools().values()) {
			final MinProducedItemsListener listener = new MinProducedItemsListener();
			listener.setCondition(this);
			tool.addListener(listener);
			int initialValue = 0;
			if (tool.getCurrentSetupState() == this.state) {
				initialValue = -this.count;
			}
			this.toolMap.put(tool, initialValue);
		}
	}

	public void notifyOfProcessStartAt(final AbstractComponent component) {
		final AbstractTool tool = (AbstractTool) component;
		this.toolMap.put(tool, this.toolMap.get(tool) + 1);
	}

	public void notifyOfSetupFinishedAt(final AbstractComponent component) {
		final AbstractTool tool = (AbstractTool) component;
		int initialValue = 0;
		if (tool.getCurrentSetupState() == this.state) {
			initialValue = -this.count;
		}
		this.toolMap.put(tool, initialValue);
	}

}
