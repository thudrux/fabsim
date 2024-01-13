package de.terministic.fabsim.metamodel.components;

public class ReworkDetails implements ProcessDetails {
	private double reworkProbability;
	private int reworkStepNumber;

	public ReworkDetails(double probability, int reworkStepNumber) {
		this.reworkProbability = probability;
		this.reworkStepNumber = reworkStepNumber;
	}

	public double getReworkProbability() {
		return reworkProbability;
	}

	public void setReworkProbability(double reworkProbability) {
		this.reworkProbability = reworkProbability;
	}

	public int getReworkStepNumber() {
		return reworkStepNumber;
	}

	public void setReworkStepNumber(int reworkStepNumber) {
		this.reworkStepNumber = reworkStepNumber;
	}

}
