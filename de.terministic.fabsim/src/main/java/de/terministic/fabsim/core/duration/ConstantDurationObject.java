package de.terministic.fabsim.core.duration;

public class ConstantDurationObject extends AbstractDurationObject {

	private final long duration;

	public ConstantDurationObject(final long duration) {
		this.duration = duration;
	}

	@Override
	public long getDuration() {
		return this.duration;
	}

	@Override
	public long getAvgDuration() {
		return this.duration;
	}

}
