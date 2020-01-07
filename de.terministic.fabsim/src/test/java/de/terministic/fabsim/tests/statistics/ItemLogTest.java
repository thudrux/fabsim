package de.terministic.fabsim.tests.statistics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.Source;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.ItemLog;

public class ItemLogTest {

	@Test
	public void itemLogBasicTest() {

		FabModel model = new FabModel();
		final EventListManager eventList = new EventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);

		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT, recipe);
		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe);
		Source source = (Source) model.getSimComponentFactory().createSource("Source1", product1, 8L);

		ItemLog log = new ItemLog();
		engine.init(model);
		engine.addListener(log);
		engine.runSimulation(50L);

		Assertions.assertEquals(6, log.getItems().size());
		Assertions.assertEquals(1L, log.getItems().get(0).getId());
	}

}
