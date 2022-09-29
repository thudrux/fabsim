package de.terministic.fabsim.tests.coretests;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.equipment.BreakdownTriggeredEvent;
import de.terministic.fabsim.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.components.equipment.breakdown.SimTimeBasedBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.SimTimeBasedMaintenance;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.duration.ConstantDuration;
import de.terministic.fabsim.core.eventlist.EventComparator;

public class EventComparatorTest {

	@Test
	public void orderTest1() {
		FabModel model = new FabModel();
		ISimEvent event1 = new BreakdownTriggeredEvent(model, 0L, null,
				new SimTimeBasedBreakdown(model, "breakdown", null, null));
		ConstantDuration obj1 = new ConstantDuration(1L);
		ConstantDuration obj10 = new ConstantDuration(10L);
		ISimEvent event2 = new MaintenanceTriggeredEvent(model, 0L, null,
				new SimTimeBasedMaintenance(model, "Maintenance", obj1, obj10));
		ArrayList<ISimEvent> events = new ArrayList<>();
		events.add(event1);
		events.add(event2);
		Collections.sort(events, new EventComparator());

		Assertions.assertEquals(2, events.size());
		Assertions.assertEquals(event1, events.get(0));
		Assertions.assertEquals(event2, events.get(1));
	}

	@Test
	public void orderTest2() {
		FabModel model = new FabModel();
		ISimEvent event1 = new BreakdownTriggeredEvent(model, 0L, null,
				new SimTimeBasedBreakdown(model, "breakdown", null, null));
		ConstantDuration obj1 = new ConstantDuration(1L);
		ConstantDuration obj10 = new ConstantDuration(10L);
		ISimEvent event2 = new MaintenanceTriggeredEvent(model, 0L, null,
				new SimTimeBasedMaintenance(model, "Maintenance", obj1, obj10));
		ArrayList<ISimEvent> events = new ArrayList<>();
		events.add(event2);
		events.add(event1);
		Collections.sort(events, new EventComparator());

		Assertions.assertEquals(2, events.size());
		Assertions.assertEquals(event1, events.get(0));
		Assertions.assertEquals(event2, events.get(1));
	}

}
