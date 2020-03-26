package de.terministic.fabsim.tests.runtimetests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.core.ComponentComparator;
import de.terministic.fabsim.core.ComponentGroupedEventListManager;
import de.terministic.fabsim.core.EventListTypeManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.TimeGroupedEventListManager;

public class EmptyFabRuntimeTest {

	private FabModel buildModel() {
		FabModel model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		model.getSimComponentFactory().createSource("Source1", product, 10L);
		return model;
	}

	@Test
	public void runTimeForEmptyModelWithTimeGroupedEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			TimeGroupedEventListManager eventList = new TimeGroupedEventListManager();
			SimulationEngine engine = new SimulationEngine();
			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 5000);
	}

	@Test
	public void runTimeForEmptyModelWithTypeGroupedEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			EventListTypeManager eventList = new EventListTypeManager(new ComponentComparator());
			SimulationEngine engine = new SimulationEngine();
			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 5000);
	}

	@Test
	public void runTimeForEmptyModelWithComponentGroupedEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			ComponentGroupedEventListManager eventList = new ComponentGroupedEventListManager();
			SimulationEngine engine = new SimulationEngine();
			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 5000);
	}

}
