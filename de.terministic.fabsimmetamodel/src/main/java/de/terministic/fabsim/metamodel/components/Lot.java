package de.terministic.fabsim.metamodel.components;

import java.util.HashSet;
import java.util.LinkedHashSet;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;

public class Lot extends AbstractFlowItem {

	private int originalLotSize;
	private final Lot originalLot;
	private int currentLotSize;
	private int prio;
	private long dueDate;
	private boolean splitLotAlreadyRemerged = false;
	private final HashSet<Lot> children = new LinkedHashSet<>();

	public Lot(final FabModel model, final Product product) {
		this(model, product, 1, 1, Long.MAX_VALUE, 1, null);
	}

	public Lot(final FabModel model, final Product product, final int originalLotSize, final int prio,
			final long dueDate) {
		this(model, product, originalLotSize, prio, Long.MAX_VALUE, originalLotSize, null);
	}

	protected Lot(final FabModel model, final Product product, final int originalLotSize, final int prio,
			final long dueDate, final int currentLotSize, final Lot originalLot) {
		super(model);
		setType(FlowItemType.LOT);
		this.product = product;
		this.setRecipe(product.getRecipe());
		this.originalLotSize = originalLotSize;
		this.prio = prio;
		this.dueDate = dueDate;
		this.currentLotSize = currentLotSize;
		this.originalLot = originalLot;
	}

	private void addChild(final Lot newChild) {
		this.children.add(newChild);
	}

	public int getCurrentLotSize() {
		mergedCheck();
		return this.currentLotSize;
	}

	public long getDueDate() {
		mergedCheck();
		return this.dueDate;
	}

	public Lot getOriginalLot() {
		mergedCheck();
		return this.originalLot;
	}

	public int getOriginalLotSize() {
		mergedCheck();
		return this.originalLotSize;
	}

	public int getPrio() {
		mergedCheck();
		return this.prio;
	}

	@Override
	public int getSize() {
		return this.currentLotSize;
	}

	private void mergedCheck() {
		if (this.splitLotAlreadyRemerged)
			throw new InvalidLotException("Lot has already been merged");
	}

	public Lot mergeWithLot(final Lot lot) {
		mergedCheck();

		if (getOriginalLot() != lot.getOriginalLot()) {
			if (this.originalLot == null && lot.originalLot != this)
				throw new InvalidLotException("Trying to merge two lots with different origins");
		} else {
			if (this.originalLot == null && lot.getOriginalLot() == null)
				throw new InvalidLotException("Trying to merge two lots with different origins");
		}
		if (lot.getCurrentStepNumber() != this.getCurrentStepNumber())
			throw new InvalidLotException("Different Step Numbers");

		if (lot.getOriginalLot() == null)
			return lot.mergeWithLot(this);
		else {
			this.currentLotSize += lot.getCurrentLotSize();
			lot.setMerged();
			Lot baseLot;
			if (this.originalLot == null) {
				baseLot = this;
			} else {
				baseLot = this.originalLot;
			}
			baseLot.children.remove(lot);
			return this;
		}
	}

	public void setDueDate(final long dueDate) {
		mergedCheck();
		this.dueDate = dueDate;
	}

	private void setMerged() {
		this.splitLotAlreadyRemerged = true;
	}

	public void setOriginalLotSize(final int originalLotSize) {
		mergedCheck();
		this.originalLotSize = originalLotSize;
	}

	public void setPrio(final int prio) {
		mergedCheck();
		this.prio = prio;
	}

	public Lot splitOfChild(final int numberOfWafer) {
		mergedCheck();
		if (this.currentLotSize < numberOfWafer)
			throw new InvalidLotSplitException("Can't split off all or more wafer than currently in a lot");
		else {
			Lot baseLot;
			if (this.originalLot == null) {
				baseLot = this;
			} else {
				baseLot = this.originalLot;
			}

			final Lot result = new Lot((FabModel) getModel(), getProduct(), this.originalLotSize, this.prio, this.dueDate,
					numberOfWafer, baseLot);
			result.setupForSimulation(getSimulationEngine());
			result.setCurrentStepNumber(this.getCurrentStepNumber());
			baseLot.addChild(result);
			result.setTimeStampMap(this.getTimeStampMap());
			this.currentLotSize = this.currentLotSize - numberOfWafer;
			return result;
		}
	}

	@Override
	public String toString() {
		String result = super.toString();
		result = result + "_" + currentLotSize;
		return result;
	}
}