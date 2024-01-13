package de.terministic.fabsim.metamodel;

public class TimeStamps {

	private int currentStep;
	private long startProcessingTime;
	private long finishProcessingTime;
	private long arrivalTime;
	private long departureTime;

	public TimeStamps(final int currentStep) {
		this.setCurrentStep(currentStep);
	}

	public long getArrivalTime() {
		return this.arrivalTime;
	}

	public int getCurrentStep() {
		return this.currentStep;
	}

	public long getDepartureTime() {
		return this.departureTime;
	}

	public long getFinishProcessingTime() {
		return this.finishProcessingTime;
	}

	public long getStartProcessingTime() {
		return this.startProcessingTime;
	}

	public void setArrivalTime(final long i) {
		this.arrivalTime = i;
	}

	public void setCurrentStep(final int currentStep) {
		this.currentStep = currentStep;
	}

	public void setDepartureTime(final long i) {
		this.departureTime = i;
	}

	public void setFinishProcessingTime(final long i) {
		this.finishProcessingTime = i;
	}

	public void setStartProcessingTime(final long i) {
		this.startProcessingTime = i;
	}
}
