package de.terministic.fabsim.metamodel.components.equipment.breakdown;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;

public class SimTimeBasedBreakdown extends AbstractBreakdown {
	private final IValue timeBetweenFailures;

	public SimTimeBasedBreakdown(FabModel model, final String name, final IValue timeToRepair,
			final IValue timeBetweenFailures) {
		super(model, name, timeToRepair);
		this.timeBetweenFailures = timeBetweenFailures;
	}

	@Override
	protected long calculateNextOccurrenceOnTool(final AbstractResource resource) {
		return getTime() + this.timeBetweenFailures.getValue();

	}

	@Override
	public long getDuration() {
		return this.duration.getValue();
	}

	@Override
	public long getFirstDefaultOccurrence() {
		if (this.defaultFirstOccurance == 0L)
			return this.timeBetweenFailures.getValue();
		else
			return super.getFirstDefaultOccurrence();
	}

	public IValue getTimeBetweenBreakdowns() {
		return this.timeBetweenFailures;
	}

	@Override
	public double getAvgCycleLength() {
		return this.timeBetweenFailures.getAvgValue() + this.duration.getAvgValue();
	}

	@Override
	public double getAvgDownTimePerCycle() {
		return this.duration.getAvgValue();

	}

}
