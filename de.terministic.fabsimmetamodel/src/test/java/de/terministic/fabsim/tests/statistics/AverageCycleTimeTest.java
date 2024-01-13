package de.terministic.fabsim.tests.statistics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.statistics.CycleTimeTracker;

public class AverageCycleTimeTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
	AbstractToolGroup toolGroup;
	ProcessStep tgStep;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		toolGroup = model.getSimComponentFactory().createToolGroup("Toolgroup");
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		tgStep = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, ProcessType.LOT,
				recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe);
		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product1, 10L);
		Product product2 = model.getSimComponentFactory().createProduct("Product2", recipe);
		source2 = (LotSource) model.getSimComponentFactory().createSource("Source2", product2, 30L);
		engine = new SimulationEngine();
	}

	@AfterEach
	public void tearDown() throws Exception {
		model = null;
		engine = null;
		toolGroup = null;
		tgStep = null;
		source = null;
	}

	@Test
	public void averageCycleTimeTest() {
		CycleTimeTracker tracker = new CycleTimeTracker();
		engine.init(model);
		engine.addListener(tracker);
		engine.runSimulation(100L);

		// model gets lots every 10L time units, and every 30L one extra lot
		// so after 100L, 12 lots have left with an average time of 6.25:
		Assertions.assertEquals(6L, tracker.getAverageCycleTime());
	}

}
