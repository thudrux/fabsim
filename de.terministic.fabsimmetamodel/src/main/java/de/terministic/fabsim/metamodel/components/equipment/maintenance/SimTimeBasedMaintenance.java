package de.terministic.fabsim.metamodel.components.equipment.maintenance;

import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IDuration;

public class SimTimeBasedMaintenance extends AbstractMaintenance {

	private final IDuration time;

	public SimTimeBasedMaintenance(FabModel model, final String name, final IDuration duration,
			final IDuration time) {
		super(model, name, duration);
		this.time = time;
	}

	@Override
	protected long calculateNextOccuranceOnTool(final AbstractResource resource) {
		this.logger.trace("calculateNextOccuranceOnTool is called for {}", resource);
		return getTime() + this.time.getDuration();

	}

	@Override
	public long getFirstDefaultOccurance() {
		if (this.defaultFirstOccurance == 0L)
			return this.time.getDuration();
		else
			return super.getFirstDefaultOccurance();
	}

	public IDuration getTimeBetweenMaintenances() {
		return this.time;
	}

	@Override
	public double getAvgCycleLength() {
		return this.time.getAvgDuration() + this.duration.getAvgDuration();
	}

	@Override
	public double getAvgDownTimePerCycle() {
		return this.duration.getAvgDuration();

	}

}
