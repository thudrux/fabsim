package de.terministic.fabsim.tests.statistics;

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
import de.terministic.fabsim.statistics.CycleTimeTracker;
import de.terministic.fabsim.statistics.FirstCycleTimeTracker;
import de.terministic.fabsim.statistics.FlowItemCounter;

public class AverageCycleTimeTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
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
		Product product1=model.getSimComponentFactory().createProduct("Product1", recipe);
		source = (LotSource)model.getSimComponentFactory().createSource("Source1", product1, 10L);
		Product product2=model.getSimComponentFactory().createProduct("Product2", recipe);
		source2 = (LotSource)model.getSimComponentFactory().createSource("Source2", product2, 30L);
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
	public void averageCycleTimeTest() {
		CycleTimeTracker tracker = new CycleTimeTracker();
		engine.init(model);
		engine.addListener(tracker);
		engine.runSimulation(100L);
		
		//model gets lots every 10L time units, and every 30L one extra lot
		//so after 100L, 12 lots have left with an average time of 6.25:
		assertEquals(6L, tracker.getAverageCycleTime());
	}

}
