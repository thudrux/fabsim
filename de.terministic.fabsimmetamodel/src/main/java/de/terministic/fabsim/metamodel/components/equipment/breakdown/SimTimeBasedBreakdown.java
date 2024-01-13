package de.terministic.fabsim.metamodel.components.equipment.breakdown;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IDuration;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;

public class SimTimeBasedBreakdown extends AbstractBreakdown {
	private final IDuration timeBetweenFailures;

	public SimTimeBasedBreakdown(FabModel model, final String name, final IDuration timeToRepair,
			final IDuration timeBetweenFailures) {
		super(model, name, timeToRepair);
		this.timeBetweenFailures = timeBetweenFailures;
	}

	@Override
	protected long calculateNextOccuranceOnTool(final AbstractResource resource) {
		return getTime() + this.timeBetweenFailures.getDuration();

	}

	@Override
	public long getDuration() {
		return this.duration.getDuration();
	}

	@Override
	public long getFirstDefaultOccurance() {
		if (this.defaultFirstOccurance == 0L)
			return this.timeBetweenFailures.getDuration();
		else
			return super.getFirstDefaultOccurance();
	}

	public IDuration getTimeBetweenBreakdowns() {
		return this.timeBetweenFailures;
	}

	@Override
	public double getAvgCycleLength() {
		return this.timeBetweenFailures.getAvgDuration() + this.duration.getAvgDuration();
	}

	@Override
	public double getAvgDownTimePerCycle() {
		return this.duration.getAvgDuration();

	}

}
