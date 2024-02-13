package de.terministic.fabsim.tests.modeltests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.statistics.FirstCycleTimeTracker;

public class MM1ModelTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
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
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product, 10L);

		engine = new FabSimulationEngine();
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
	public void cycleTimeTestWithZeroProcessingTime() {
		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		long duration = 0L;
		tgStep.setDuration(model.getValueObjectFactory().createConstantValueObject(duration));
		engine.init(model);
		engine.addListener(tracker);
//		engine.addListener(new SimulationLog());
		engine.runSimulation(100L);
		Assertions.assertEquals(duration, tracker.getCycleTime());
	}

	@Test
	public void cycleTimeTestWithNonZeroProcessingTime() {
		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		long duration = 5L;
		tgStep.setDuration(model.getValueObjectFactory().createConstantValueObject(duration));
		engine.init(model);
		engine.addListener(tracker);
		engine.runSimulation(100L);
		Assertions.assertEquals(duration, tracker.getCycleTime());
	}

}
