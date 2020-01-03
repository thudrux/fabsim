package de.terministic.fabsim.components.equipment.breakdown;

import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.duration.AbstractDurationObject;

public class SimTimeBasedBreakdown extends AbstractBreakdown {
	private final AbstractDurationObject timeBetweenFailures;

	public SimTimeBasedBreakdown(FabModel model, final String name, final AbstractDurationObject timeToRepair,
			final AbstractDurationObject timeBetweenFailures) {
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

	public AbstractDurationObject getTimeBetweenBreakdowns() {
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
