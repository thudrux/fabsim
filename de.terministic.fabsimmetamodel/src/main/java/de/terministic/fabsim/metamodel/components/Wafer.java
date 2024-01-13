package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;

public class Wafer extends AbstractFlowItem {

	private Lot originalLot;

	public Wafer(final FabModel model, final Lot originalLot) {
		super(model);
		this.originalLot = originalLot;
		setType(FlowItemType.WAFER);
	}

	public Wafer(final FabModel model, final Lot originalLot, final Recipe recipe) {
		super(model);
		this.setRecipe(recipe);
		this.originalLot = originalLot;
		setType(FlowItemType.WAFER);

	}

	public Lot getOriginalLot() {
		return this.originalLot;
	}

	@Override
	public int getSize() {
		return 1;
	}

	public void setOriginalLot(final Lot originalLot) {
		this.originalLot = originalLot;
	}
}
