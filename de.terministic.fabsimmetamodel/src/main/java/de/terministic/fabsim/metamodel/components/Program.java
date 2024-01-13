package de.terministic.fabsim.metamodel.components;

public class Program {
	private String name;
	private long duration;

	public long getDuration() {
		return this.duration;
	}

	public String getName() {
		return this.name;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
