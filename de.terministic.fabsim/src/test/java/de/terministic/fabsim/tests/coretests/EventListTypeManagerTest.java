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
			manager.scheduleEvent(new ProcessFinishedEvent(model, 13L, toolGroup, null, null));
			manager.scheduleEvent(new ProcessFinishedEvent(model, 12L, toolGroup, null, null));
			manager.scheduleEvent(new ProcessFinishedEvent(model, 14L, toolGroup, null, null));
			manager.scheduleEvent(new ProcessFinishedEvent(model, 11L, toolGroup, null, null));
			manager.scheduleEvent(new OperatorFinishedEvent(model, 11L, source, null, null));
			manager.scheduleEvent(new OperatorFinishedEvent(model, 12L, source, null, null));
			manager.scheduleEvent(new OperatorFinishedEvent(model, 10L, source, null, null));
			manager.scheduleEvent(new ProcessFinishedEvent(model, 10L, toolGroup, null, null));
			manager.scheduleEvent(new ProcessFinishedEvent(model, 13L, toolGroup, null, null));

			AbstractSimEvent nextEvent = manager.getNextEvent();
			System.out.println(Long.toString(nextEvent.getEventTime()));
			while (nextEvent != null) {
				nextEvent = manager.getNextEvent();
				System.out.println(nextEvent.getClass().toString() + " " + Long.toString(nextEvent.getEventTime()) + " "
						+ Integer.toString(nextEvent.getPriority()) + " " + Long.toString(nextEvent.getId()));
			}

		} catch (Exception e) {
			return;
		}

		return;
	}
}
