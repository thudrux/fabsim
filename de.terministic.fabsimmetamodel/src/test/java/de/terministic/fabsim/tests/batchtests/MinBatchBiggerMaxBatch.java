package de.terministic.fabsim.tests.batchtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

/*
 * Test, if you can set a bigger minBatch than maxBatch
 */

public class MinBatchBiggerMaxBatch {

	ToolGroup toolGroup;

	@Test
	public void MinBatchBiggerMaxBatch() {

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final BatchDetails details = new BatchDetails("Typ1", 10, 5);// (Name, minBatch, maxBatch)
		});
	}

}