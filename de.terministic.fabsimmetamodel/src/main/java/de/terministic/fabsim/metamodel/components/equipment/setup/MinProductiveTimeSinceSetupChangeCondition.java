package de.terministic.fabsim.metamodel.components.equipment.setup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.NotYetImplementedException;

public class MinProductiveTimeSinceSetupChangeCondition implements ISetupChangeCondition {
	private final Map<AbstractTool, Long> toolMap = new LinkedHashMap<>();
	private final SetupState state;
	private final long minProdTime;

	public MinProductiveTimeSinceSetupChangeCondition(final SetupState state, final long minProdTime) {
		this.state = state;
		this.minProdTime = minProdTime;
	}

	@Override
	public boolean checkCondition(final AbstractTool tool, final AbstractFlowItem item, final SetupState newState) {
		return this.toolMap.get(tool) >= 0L;
	}

	@Override
	public boolean checkCondition(final AbstractToolGroup toolGroup, final List<AbstractFlowItem> flowItems,
			final AbstractTool tool, final SetupState newState) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	public long getMinProdTime() {
		return this.minProdTime;
	}

	@Override
	public SetupState getState() {
		return this.state;
	}

	@Override
	public void initialize(final AbstractToolGroup toolGroup) {
		for (final AbstractTool tool : toolGroup.getTools().values()) {
			final MinProductiveTimeListener listener = new MinProductiveTimeListener();
			listener.setCondition(this);
			tool.addListener(listener);
			long initialValue = 0L;
			if (tool.getCurrentSetupState() == this.state) {
				initialValue = -this.minProdTime;
			}
			this.toolMap.put(tool, initialValue);
		}
	}

	public void notifyOfProcessStartAt(final AbstractComponent component, final AbstractFlowItem item) {
		final AbstractTool tool = (AbstractTool) component;
		this.toolMap.put(tool, this.toolMap.get(tool) + item.getCurrentStep().getDuration(item));
	}

	public void notifyOfSetupFinishedAt(final AbstractComponent component) {
		final AbstractTool tool = (AbstractTool) component;
		long initialValue = 0L;
		if (tool.getCurrentSetupState() == this.state) {
			initialValue = -this.minProdTime;
		}
		this.toolMap.put(tool, initialValue);
	}
}
