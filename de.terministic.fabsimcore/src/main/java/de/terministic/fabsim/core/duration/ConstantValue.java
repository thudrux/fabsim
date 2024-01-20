package de.terministic.fabsim.core.duration;

public class ConstantValue implements IValue {

	private final long duration;

	public ConstantValue(final long duration) {
		this.duration = duration;
	}

	@Override
	public long getValue() {
		return this.duration;
	}

	@Override
	public long getAvgValue() {
		return this.duration;
	}

}
