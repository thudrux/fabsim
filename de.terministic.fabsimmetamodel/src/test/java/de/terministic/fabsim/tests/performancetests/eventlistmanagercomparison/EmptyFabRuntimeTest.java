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

@Disabled("Disabled, includes only performance tests")
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
	@Tag("slow")
	public void runTimeForEmptyModelWithComponentGroupedEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			ComponentGroupedEventListManager eventList = new ComponentGroupedEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 5000);
	}

	@Test
	@Tag("slow")
	public void runTimeForEmptyModelWithPriorityQueueEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 5000);
	}

	@Test
	@Tag("slow")
	public void runTimeForEmptyModelWithTreeSetEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			TreeSetEventListManager eventList = new TreeSetEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(10000000L);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 5000);
	}

}
