package de.terministic.fabsim.tests.runtimetests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.ComponentGroupedEventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.PriorityQueueEventListManager;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.TreeSetEventListManager;

public class MiniFabRuntimeTest {

	private FabModel buildModel() {
		FabModel model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		AbstractToolGroup toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup");
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		model.getSimComponentFactory().createSource("Source1", product, 10L);
		return model;
	}

	@Test
	@Tag("slow")
	public void runTimeForMiniModelWithComponentGroupedEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();

			ComponentGroupedEventListManager eventList = new ComponentGroupedEventListManager();

			SimulationEngine engine = new SimulationEngine(eventList);

			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 11000);
	}

	@Test
	@Tag("slow")
	public void runTimeForMiniModelWithPriorityQueueEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();

			PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();

			SimulationEngine engine = new SimulationEngine(eventList);

			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 11000);
	}

	@Test
	@Tag("slow")
	public void runTimeForMiniModelWithTreeSetEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();

			TreeSetEventListManager eventList = new TreeSetEventListManager();

			SimulationEngine engine = new SimulationEngine(eventList);

			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 11000);
	}
}
