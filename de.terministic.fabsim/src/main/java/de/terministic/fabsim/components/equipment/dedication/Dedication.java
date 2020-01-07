package de.terministic.fabsim.components.equipment.dedication;

public class Dedication {
	public Dedication(String name) {
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name;

}
