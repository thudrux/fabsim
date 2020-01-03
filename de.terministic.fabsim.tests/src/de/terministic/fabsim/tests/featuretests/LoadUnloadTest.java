package de.terministic.fabsim.tests.featuretests;

import static org.junit.Assert.*;

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
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractOperatorGroup;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FirstCycleTimeTracker;

public class LoadUnloadTest {

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
		tgStep = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup,null, 3L,5L, 7L,null, null, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT,recipe);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);

		source = (LotSource)model.getSimComponentFactory().createSource("Source1", product, 200L);
		source.setCreateFirstAtTimeZero(true);

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
	public void cycleTimeWithLoadAndUnloadTime() {
		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		long duration =3L+5L+7L;
		engine.init(model);
		engine.addListener(tracker);
		engine.runSimulation(50L);
		assertEquals(duration, tracker.getCycleTime());
	}

}
