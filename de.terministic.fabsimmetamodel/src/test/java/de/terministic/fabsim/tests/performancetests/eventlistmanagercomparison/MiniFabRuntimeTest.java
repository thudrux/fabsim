package de.terministic.fabsim.tests.performancetests.eventlistmanagercomparison;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.eventlist.ComponentGroupedEventListManager;
import de.terministic.fabsim.core.eventlist.PriorityQueueEventListManager;
import de.terministic.fabsim.core.eventlist.TreeSetEventListManager;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

@Disabled("Disabled, includes only performance tests")
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

			SimulationEngine engine = new FabSimulationEngine(eventList);

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

			SimulationEngine engine = new FabSimulationEngine(eventList);

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

			SimulationEngine engine = new FabSimulationEngine(eventList);

			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 11000);
	}
}
