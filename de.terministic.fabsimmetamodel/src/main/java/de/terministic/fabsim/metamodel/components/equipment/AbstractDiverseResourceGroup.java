package de.terministic.fabsim.metamodel.components.equipment;

import java.util.List;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;

public class AbstractDiverseResourceGroup extends AbstractToolGroup {

	public AbstractDiverseResourceGroup(FabModel model, String name, AbstractToolGroupController tgController) {
		super(model, name, tgController);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void becomesAvailable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void becomesUnavailable() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AbstractResource> getGroupMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onResourceBecomesAvailable(AbstractResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResourceBecomesUnavailable(AbstractResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void takeAfterProcessing(AbstractFlowItem flowItem, AbstractResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addBreakdown(IBreakdown breakdown) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canProcessItem() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void announceFlowItemArrival(AbstractFlowItem item) {
		// TODO Auto-generated method stub

	}

}
