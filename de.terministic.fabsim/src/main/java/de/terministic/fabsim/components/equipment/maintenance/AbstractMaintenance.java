package de.terministic.fabsim.components.equipment.maintenance;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.terministic.fabsim.components.InvalidDataException;
import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.IDuration;

public abstract class AbstractMaintenance extends AbstractModelElement implements IMaintenance {
	protected String name;
	protected IDuration duration;
	protected HashMap<AbstractResource, Long> nextOccuranceOnTool = new LinkedHashMap<>();
	protected long defaultFirstOccurance = 0L;

	protected FabModel model;

	public AbstractMaintenance(FabModel model, final String name, final IDuration duration2) {
		super(model);
		this.name = name;
		this.duration = duration2;
	}

	@Override
	public void addTool(final AbstractResource resource) {
		this.nextOccuranceOnTool.put(resource, getFirstDefaultOccurance());
	}

	protected abstract long calculateNextOccuranceOnTool(AbstractResource resource);

	@Override
	public long getDuration() {
		return this.duration.getDuration();
	}

	@Override
	public FabModel getFabModel() {
		return this.model;
	}

	public long getFirstDefaultOccurance() {
		return this.defaultFirstOccurance;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public long getNextOccuranceOnTool(final AbstractResource resource) {
		return this.nextOccuranceOnTool.get(resource);
	}

	@Override
	public long getTimeOfFirstOccurance(final AbstractResource resource) {
		return this.defaultFirstOccurance;
	}

	@Override
	public long getTimeOfNextOccurance(final AbstractResource resource) {
		return this.nextOccuranceOnTool.get(resource);
	}

	@Override
	public long getTimeTillNextOccuranceOnTool(final AbstractResource resource) {
		final long occurance = this.nextOccuranceOnTool.get(resource);
		return occurance - getTime();
	}

	@Override
	public void initialize() {
		for (final AbstractResource resource : this.nextOccuranceOnTool.keySet()) {
			final SimulationEngine engine = this.model.getSimulationEngine();
			engine.getEventFactory().scheduleNewMaintenanceTriggeredEvent(resource, this);
		}

	}

	@Override
	public void maintenanceFinished(final AbstractResource resource) {
		final long nextOccurance = calculateNextOccuranceOnTool(resource);
		// this.logger.debug("Next occurance of {} at {} will be at {} ", this,
		// resource, nextOccurance);
		this.nextOccuranceOnTool.put(resource, nextOccurance);
		this.model.getSimulationEngine().getEventFactory().scheduleNewMaintenanceTriggeredEvent(resource, this);
	}

	public void setDuration(final IDuration duration) {
		this.duration = duration;
	}

	@Override
	public void setFabModel(final FabModel model) {
		this.model = model;
	}

	public void setFirstDefaultOccurance(final long firstDefaultOccurance) {
		this.defaultFirstOccurance = firstDefaultOccurance;
	}

	public void setFirstOccuranceForTool(final AbstractResource resource, final long firstOccurance) {
		if (this.nextOccuranceOnTool.containsKey(resource)) {
			this.nextOccuranceOnTool.put(resource, firstOccurance);
		} else
			throw new InvalidDataException("Resource " + resource.getName() + " is not covered by " + this.getName());
	}

	public void setName(final String name) {
		this.name = name;
	}

	protected void setNextOccuranceOnTool(final AbstractResource resource, final long time) {
		this.nextOccuranceOnTool.put(resource, time);
	}

	@Override
	public String toString() {
		return this.name;
	}

}
