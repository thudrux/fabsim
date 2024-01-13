package de.terministic.fabsim.core.duration;

public class ConstantDuration implements IDuration {

	private final long duration;

	public ConstantDuration(final long duration) {
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
