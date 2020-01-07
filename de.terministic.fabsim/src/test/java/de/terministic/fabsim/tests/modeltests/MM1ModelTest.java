package de.terministic.fabsim.tests.modeltests;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessStep;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.ConstantDurationObject;
import de.terministic.fabsim.statistics.FirstCycleTimeTracker;

public class MM1ModelTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	AbstractToolGroup toolGroup;
	ProcessStep tgStep;
	
	@Before
	public void setUp() throws Exception {
		model = new FabModel();
		Sink sink = (Sink)model.getSimComponentFactory().createSink();
		toolGroup=model.getSimComponentFactory().createToolGroup("Toolgroup");
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		tgStep = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, ProcessType.LOT,recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT,recipe);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);

		source = (LotSource)model.getSimComponentFactory().createSource("Source1", product, 10L);

		EventListManager eventList= new EventListManager();
		engine = new SimulationEngine(eventList);
	}

	@After
	public void tearDown() throws Exception {
		model = null;
		engine= null;
		toolGroup=null;
		tgStep=null;
		source=null;
	}

	@Test
	public void cycleTimeTestWithZeroProcessingTime() {
		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		long duration =0L;
		tgStep.setDuration(model.getDurationObjectFactory().createConstantDurationObject(duration));
		engine.init(model);
		engine.addListener(tracker);
//		engine.addListener(new SimulationLog());
		engine.runSimulation(100L);
		assertEquals(duration, tracker.getCycleTime());
	}

	@Test
	public void cycleTimeTestWithNonZeroProcessingTime() {
		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		long duration =5L;
		tgStep.setDuration(model.getDurationObjectFactory().createConstantDurationObject(duration));
		engine.init(model);
		engine.addListener(tracker);
		engine.runSimulation(100L);
		assertEquals(duration, tracker.getCycleTime());
	}

}
