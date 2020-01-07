package de.terministic.fabsim.components.equipment.dedication;

import de.terministic.fabsim.components.ProcessDetails;

public class DedicationDetails implements ProcessDetails {

	private Dedication necessaryQualification;

	public DedicationDetails(Dedication dedication) {
		this.setNecessaryQualification(dedication);
	}

	public Dedication getNecessaryQualification() {
		return necessaryQualification;
	}

	public void setNecessaryQualification(Dedication necessaryQualification) {
		this.necessaryQualification = necessaryQualification;
	}

}
