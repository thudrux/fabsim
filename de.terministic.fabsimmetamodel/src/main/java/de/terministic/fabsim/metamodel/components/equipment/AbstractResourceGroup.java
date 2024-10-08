package de.terministic.fabsim.metamodel.components.equipment;

import java.util.List;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;

public abstract class AbstractResourceGroup extends AbstractResource {

	public AbstractResourceGroup(FabModel model, final String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	public abstract List<AbstractResource> getGroupMembers();

	public abstract void onResourceBecomesAvailable(AbstractResource resource);

	public abstract void onResourceBecomesUnavailable(AbstractResource resource);

	public abstract void takeAfterProcessing(AbstractFlowItem flowItem, AbstractResource resource);
}
