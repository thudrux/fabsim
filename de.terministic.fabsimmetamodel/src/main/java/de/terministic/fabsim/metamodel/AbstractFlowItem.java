package de.terministic.fabsim.metamodel;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.core.IModel;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.dispatchRules.MaxWaitingTimeInQueueEvent;

public abstract class AbstractFlowItem extends AbstractModelElement implements IFlowItem {

	private final IModel model;

	public enum FlowItemType {
		WAFER, LOT, BATCH
	};

	private FlowItemType type;

	protected long id;
	protected Product product;

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	protected Recipe recipe;
	private AbstractTool reservedTool;
	private HashMap<String, Long> statistics = new LinkedHashMap<>();

	protected int currentStepNumber = 0;

	private long creationTime;
	private LinkedHashMap<Integer, TimeStamps> timeStampMap = new LinkedHashMap<>();

	public AbstractFlowItem(final IModel model) {
		super(model);
		this.model = model;
		this.id = model.getNextFlowItemId();
	}

	public void addTimeStamps(final Integer stepNumber, final TimeStamps timeStamps) {
		this.timeStampMap.put(stepNumber, timeStamps);
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public ProcessStep getCurrentStep() {
		return this.recipe.get(this.currentStepNumber);
	}

	public int getCurrentStepNumber() {
		return this.currentStepNumber;
	}

	public TimeStamps getCurrentTimeStemp() {
		return getTimeStamps(this.currentStepNumber);
	}

	@Override
	public long getId() {
		return this.id;
	}

	public IModel getModel() {
		return this.model;
	}

	public Recipe getRecipe() {
		return this.recipe;
	}

	public AbstractTool getReservedTool() {
		return this.reservedTool;
	}

	public abstract int getSize();

	public LinkedHashMap<Integer, TimeStamps> getTimeStampMap() {
		return this.timeStampMap;
	}

	public TimeStamps getTimeStamps(final int stepNumber) {
		TimeStamps result;
		if (this.timeStampMap.containsKey(stepNumber)) {
			result = this.timeStampMap.get(stepNumber);
		} else {
			result = new TimeStamps(stepNumber);
			this.timeStampMap.put(stepNumber, result);
		}
		return result;

	}

	public void markCurrentStepAsFinished() {
		this.currentStepNumber++;
	}

	public void setCreationTime(final long creationTime) {
		this.creationTime = creationTime;
	}

	public void setCurrentStepNumber(final int currentStepNumber) {
		this.currentStepNumber = currentStepNumber;
	}

	protected void setRecipe(final Recipe recipe) {
		this.recipe = recipe;

	}

	public void setReservedTool(final AbstractTool reservedTool) {
		this.reservedTool = reservedTool;
	}

	public void setTimeStampMap(final LinkedHashMap<Integer, TimeStamps> timeStampMap) {
		this.timeStampMap = timeStampMap;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "_" + this.id + "_Recipe_" + this.recipe + "_"
				+ this.currentStepNumber;
	}

	public HashMap<String, Long> getStatistics() {
		return statistics;
	}

	public void setStatistics(HashMap<String, Long> statistics) {
		this.statistics = statistics;
	}

	public long getRPT() {
		long rpt = 0L;
		for (ProcessStep step : this.getRecipe()) {
			rpt += step.getLoadTime() + step.getDuration(this) + step.getUnloadTime();
		}
		if (rpt == 0L) {
			rpt = -1L;
		}
		return rpt;
	}

	public MaxWaitingTimeInQueueEvent getMaxWaitEvent() {
		return maxWaitEvent;
	}

	public void setMaxWaitEvent(MaxWaitingTimeInQueueEvent maxWaitEvent) {
		this.maxWaitEvent = maxWaitEvent;
	}

	private MaxWaitingTimeInQueueEvent maxWaitEvent = null;

	public void unscheduleMaxQueueTimeEvents() {
		if (maxWaitEvent != null) {
			getSimulationEngine().getEventList().unscheduleEvent(maxWaitEvent);
			maxWaitEvent = null;
		}
	}

	public FlowItemType getType() {
		return type;
	}

	public void setType(FlowItemType type) {
		this.type = type;
	}

}
