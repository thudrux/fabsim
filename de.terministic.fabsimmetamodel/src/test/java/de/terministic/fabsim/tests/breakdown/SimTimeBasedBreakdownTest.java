/* Simple Interface Tests for SimTimeBasedBreakdown
 * 
 * @author    Falk Pappert
 */
package de.terministic.fabsim.tests.breakdown;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.SimTimeBasedBreakdown;
import de.terministic.fabsim.core.duration.ConstantDurationObject;

/**
 * The Class SimTimeBasedBreakdownTest.
 */
public class SimTimeBasedBreakdownTest {

	/**
	 * Test.
	 */
	@Test
	public void creationTest() {
		IValue mttr = new ConstantDurationObject(10L);
		IValue mtbf = new ConstantDurationObject(7L);
		FabModel model = new FabModel();
		SimTimeBasedBreakdown breakdown = new SimTimeBasedBreakdown(model, "New Breakdown", mttr, mtbf);
		Assertions.assertEquals("New Breakdown", breakdown.getName());
		Assertions.assertEquals(10L, breakdown.getDuration());
		Assertions.assertEquals(7L, breakdown.getTimeBetweenBreakdowns().getValue());
	}

}
