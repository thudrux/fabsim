package de.terministic.fabsim.metamodel.components.equipment.breakdown;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.terministic.fabsim.metamodel.components.InvalidDataException;
import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.BreakdownTriggeredEvent;

public abstract class AbstractBreakdown extends AbstractModelElement implements IBreakdown {
	protected String name;
	protected IValue duration;
	protected HashMap<AbstractResource, Long> nextOccuranceOnTool = new LinkedHashMap<>();
	protected long defaultFirstOccurance = 0L;
	protected FabModel model;

	public AbstractBreakdown(FabModel model, final String name, final IValue timeToRepair) {
		super(model);
		this.name = name;
		this.duration = timeToRepair;
	}

	@Override
	public void addTool(final AbstractResource resource) {
		this.nextOccuranceOnTool.put(resource, getFirstDefaultOccurance());
	}

	@Override
	public void breakdownFinished(final AbstractResource tool) {
		final long nextOccurance = calculateNextOccuranceOnTool(tool);
		this.logger.trace("Next occurance of {} at {} will be at {} ", this, tool, nextOccurance);
		this.nextOccuranceOnTool.put(tool, nextOccurance);
		scheduleNewBreakdownTriggeredEvent(tool);
	}

	protected abstract long calculateNextOccuranceOnTool(AbstractResource resource);

	public long getFirstDefaultOccurance() {
		return this.defaultFirstOccurance;
	}

	@Override
	public FabModel getModel() {
		return this.model;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public long getNextOccuranceOnTool(final AbstractResource tool) {
		return this.nextOccuranceOnTool.get(tool);
	}

	@Override
	public long getTimeOfFirstOccurance(final AbstractResource abstractTool) {
		return this.defaultFirstOccurance;
	}

	@Override
	public long getTimeOfNextOccurance(final AbstractResource abstractTool) {
		return this.nextOccuranceOnTool.get(abstractTool);
	}

	@Override
	public long getTimeTillNextOccuranceOnTool(final AbstractResource tool) {
		final long occurance = this.nextOccuranceOnTool.get(tool);
		return occurance - getTime();
	}

	@Override
	public void initialize() {
		for (final AbstractResource resource : this.nextOccuranceOnTool.keySet()) {
			scheduleNewBreakdownTriggeredEvent(resource);
		}
	}

	public BreakdownTriggeredEvent scheduleNewBreakdownTriggeredEvent(final AbstractResource resource) {
		final BreakdownTriggeredEvent event = new BreakdownTriggeredEvent(getModel(), getTimeOfNextOccurance(resource),
				resource, this);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public BreakdownTriggeredEvent scheduleFirstBreakdownTriggeredEvent(final AbstractResource resource) {
		final BreakdownTriggeredEvent event = new BreakdownTriggeredEvent(getModel(), getTimeOfNextOccurance(resource),
				resource, this);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public void setDuration(final IValue duration) {
		this.duration = duration;
	}

	public void setFirstDefaultOccurance(final long firstDefaultOccurance) {
		this.defaultFirstOccurance = firstDefaultOccurance;
		for (AbstractResource resource : nextOccuranceOnTool.keySet()) {
			this.nextOccuranceOnTool.put(resource, firstDefaultOccurance);
		}
	}

	public void setFirstOccuranceForTool(final AbstractResource resource, final long firstOccurance) {
		if (this.nextOccuranceOnTool.containsKey(resource)) {
			this.nextOccuranceOnTool.put(resource, firstOccurance);
		} else
			throw new InvalidDataException("Tool " + resource.getName() + " is not covered by " + this.getName());
	}

	@Override
	public void setModel(final FabModel model) {
		this.model = model;
	}

	public void setName(final String name) {
		this.name = name;
	}

	protected void setNextOccuranceOnTool(final AbstractResource tool, final long time) {
		this.nextOccuranceOnTool.put(tool, time);
	}

	@Override
	public String toString() {
		return this.name;
	}

}
