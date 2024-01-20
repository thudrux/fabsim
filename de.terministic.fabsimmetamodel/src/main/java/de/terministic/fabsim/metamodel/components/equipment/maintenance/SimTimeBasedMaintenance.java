package de.terministic.fabsim.metamodel.components.equipment.maintenance;

import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IValue;

public class SimTimeBasedMaintenance extends AbstractMaintenance {

	private final IValue time;

	public SimTimeBasedMaintenance(FabModel model, final String name, final IValue duration,
			final IValue time) {
		super(model, name, duration);
		this.time = time;
	}

	@Override
	protected long calculateNextOccuranceOnTool(final AbstractResource resource) {
		this.logger.trace("calculateNextOccuranceOnTool is called for {}", resource);
		return getTime() + this.time.getValue();

	}

	@Override
	public long getFirstDefaultOccurance() {
		if (this.defaultFirstOccurance == 0L)
			return this.time.getValue();
		else
			return super.getFirstDefaultOccurance();
	}

	public IValue getTimeBetweenMaintenances() {
		return this.time;
	}

	@Override
	public double getAvgCycleLength() {
		return this.time.getAvgValue() + this.duration.getAvgValue();
	}

	@Override
	public double getAvgDownTimePerCycle() {
		return this.duration.getAvgValue();

	}

}
