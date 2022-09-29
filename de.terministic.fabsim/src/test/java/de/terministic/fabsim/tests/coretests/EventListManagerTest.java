package de.terministic.fabsim.tests.coretests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.equipment.OperatorFinishedEvent;
import de.terministic.fabsim.components.equipment.ProcessFinishedEvent;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.eventlist.PriorityQueueEventListManager;

public class EventListManagerTest {

	@Test
	public void timeTest() {
		FabModel model = new FabModel();
		PriorityQueueEventListManager manager = new PriorityQueueEventListManager();
		OperatorFinishedEvent opEvent1 = new OperatorFinishedEvent(model, 13L, null, null, null);
		manager.scheduleEvent(opEvent1);
		OperatorFinishedEvent opEvent2 = new OperatorFinishedEvent(model, 12L, null, null, null);
		manager.scheduleEvent(opEvent2);
		ISimEvent nextEvent = manager.getNextEvent();
		Assertions.assertEquals(opEvent2, nextEvent);
	}

	@Test
	public void prioTest() {
		FabModel model = new FabModel();
		PriorityQueueEventListManager manager = new PriorityQueueEventListManager();
		manager.scheduleEvent(new ProcessFinishedEvent(model, 12L, null, null, null));
		OperatorFinishedEvent opEvent = new OperatorFinishedEvent(model, 12L, null, null, null);
		manager.scheduleEvent(opEvent);
		ISimEvent nextEvent = manager.getNextEvent();
		Assertions.assertEquals(opEvent, nextEvent);
	}

}
