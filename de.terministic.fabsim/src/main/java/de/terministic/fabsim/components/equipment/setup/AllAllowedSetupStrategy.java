package de.terministic.fabsim.components.equipment.setup;

import java.util.Collection;

import de.terministic.fabsim.components.equipment.AbstractTool;
import de.terministic.fabsim.core.AbstractFlowItem;

public class AllAllowedSetupStrategy extends AbstractSetupStrategy {

	@Override
	public boolean filterForValidItem(final AbstractTool tool, final AbstractFlowItem item) {
		return true;
	}

	@Override
	public Collection<AbstractFlowItem> filterValidItems(final AbstractTool tool,
			final Collection<AbstractFlowItem> possibleItems) {
		return possibleItems;
	}

}
