package de.terministic.fabsim.core.eventlist;

import java.util.ArrayList;

import de.terministic.fabsim.core.AbstractSimEvent;

public class CalendarQueueBucket extends ArrayList<AbstractSimEvent> {

	private double bucketTop;
	private double bucketBottom;

	public double getBucketTop() {
		return bucketTop;
	}

	public void setBucketTop(double bucketTop) {
		this.bucketTop = bucketTop;
	}

	public double getBucketBottom() {
		return bucketBottom;
	}

	public void setBucketBottom(double bucketBottom) {
		this.bucketBottom = bucketBottom;
	}

}
