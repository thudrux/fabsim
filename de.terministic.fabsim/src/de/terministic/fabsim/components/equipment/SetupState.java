package de.terministic.fabsim.components.equipment;

public class SetupState {
	private String setupName;

	public SetupState(final String name) {
		this.setupName = name;
	}

	public String getSetupName() {
		return this.setupName;
	}

	public void setSetupName(final String setupName) {
		this.setupName = setupName;
	}

	@Override
	public String toString() {
		return "SetupState_" + this.setupName;
	}
}
