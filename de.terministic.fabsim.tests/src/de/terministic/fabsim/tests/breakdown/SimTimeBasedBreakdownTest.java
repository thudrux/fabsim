/* Simple Interface Tests for SimTimeBasedBreakdown
 * 
 * @author    Falk Pappert
 */
package de.terministic.fabsim.tests.breakdown;

import static org.junit.Assert.*;

import org.junit.Test;

import de.terministic.fabsim.components.equipment.breakdown.SimTimeBasedBreakdown;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.duration.AbstractDurationObject;
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
		AbstractDurationObject mttr =new ConstantDurationObject(10L);
		AbstractDurationObject mtbf =new ConstantDurationObject(7L);
		FabModel model= new FabModel();
		SimTimeBasedBreakdown breakdown = new SimTimeBasedBreakdown(model,"New Breakdown", mttr, mtbf);
		assertEquals("New Breakdown", breakdown.getName());
		assertEquals(10L, breakdown.getDuration());
		assertEquals(7L, breakdown.getTimeBetweenBreakdowns().getDuration());
	}

}
