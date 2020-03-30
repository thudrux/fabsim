package de.terministic.fabsim.tests.coretests;

import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.components.equipment.OperatorFinishedEvent;
import de.terministic.fabsim.components.equipment.ProcessFinishedEvent;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.AbstractSource;
import de.terministic.fabsim.core.ComponentComparator;
import de.terministic.fabsim.core.EventListTypeManager;
import de.terministic.fabsim.core.FabModel;

public class EventListTypeManagerTest {

	public void printEvent(AbstractSimEvent event) {
		System.out.println(event.getClass().getCanonicalName() + ", Time:" + Long.toString(event.getEventTime())
				+ ", Priority: " + Integer.toString(event.getPriority()) + ", ID:" + Long.toString(event.getId()));
	}

	@Test
	public void scheduleTest() {

		FabModel model = new FabModel();
		EventListTypeManager manager = new EventListTypeManager(new ComponentComparator());

		// EventType 1
		AbstractToolGroup toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup");
		// EventType 2
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		AbstractSource source = model.getSimComponentFactory().createSource("Source1", product, 10L);

		try {
			AbstractSimEvent event1 = new ProcessFinishedEvent(model, 13L, toolGroup, null, null);
			AbstractSimEvent event2 = new ProcessFinishedEvent(model, 12L, toolGroup, null, null);
			AbstractSimEvent event3 = new ProcessFinishedEvent(model, 15L, toolGroup, null, null);
			AbstractSimEvent event4 = new OperatorFinishedEvent(model, 15L, source, null, null);
			AbstractSimEvent event5 = new OperatorFinishedEvent(model, 14L, source, null, null);
			AbstractSimEvent event6 = new OperatorFinishedEvent(model, 11L, source, null, null);

			manager.scheduleEvent(event1);
			manager.scheduleEvent(event2);
			manager.scheduleEvent(event3);
			manager.scheduleEvent(event4);
			manager.scheduleEvent(event5);
			manager.scheduleEvent(event6);

			// manager.unscheduleEvent(event6);

			AbstractSimEvent nextEvent = manager.getNextEvent();
			printEvent(nextEvent);
			while (nextEvent != null) {
				nextEvent = manager.getNextEvent();
				printEvent(nextEvent);
			}

		} catch (Exception e) {
			return;
		}

		return;
	}
}
