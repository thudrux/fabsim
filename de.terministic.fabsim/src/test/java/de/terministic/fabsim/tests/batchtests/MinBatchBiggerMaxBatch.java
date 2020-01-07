package de.terministic.fabsim.tests.batchtests;

import org.junit.Test;

import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;

/*
 * Test, if you can set a bigger minBatch than maxBatch
 */

public class MinBatchBiggerMaxBatch {

	ToolGroup toolGroup;

	@Test(expected = IllegalArgumentException.class)
	public void MinBatchBiggerMaxBatch() {
		final BatchDetails details = new BatchDetails("typ1", 10, 5);// (Name,
																		// minBatch,
																		// maxBatch)
	}

}