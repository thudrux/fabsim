package de.terministic.fabsim.metamodel.components.equipment.setup;

import java.util.Collection;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.AbstractFlowItem;

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
