package de.terministic.fabsim.tests.durationobjecttests;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.core.duration.ExponentialDuration;

public class ExponentialDurationObjectTest {

	@Test
	public void sampleTestOneDay() {
		ExponentialDuration obj = new ExponentialDuration(86400L, new Random());
		long sum = 0L;
		int sampleSize = 10000000;
		for (int i = 0; i < sampleSize; i++) {
			sum += obj.getDuration();
		}
		double avg = (sum * 1.0) / sampleSize;
		Assertions.assertEquals(86400.0, avg, 100);
	}

	@Test
	public void avgTestOneDay() {
		ExponentialDuration obj = new ExponentialDuration(86400L, new Random());
		double avg = obj.getAvgDuration();
		Assertions.assertEquals(86400.0, avg, 0);
	}

}
