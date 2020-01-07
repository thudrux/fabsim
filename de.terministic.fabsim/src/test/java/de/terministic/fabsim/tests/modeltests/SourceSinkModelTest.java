package de.terministic.fabsim.tests.modeltests;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.Source;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FlowItemCounter;

public class SourceSinkModelTest {

	FabModel model;
	SimulationEngine engine;
	Source source;
	@Before
	public void setUp() throws Exception {
		model = new FabModel();
		Sink sink = (Sink)model.getSimComponentFactory().createSink();
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT, recipe);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);

		source = (Source)model.getSimComponentFactory().createSource("Source1", product, 10L);

		EventListManager eventList= new EventListManager();
		engine = new SimulationEngine(eventList);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void generationCountTestDefault() {
		FlowItemCounter counter = new FlowItemCounter(); 
		engine.init(model);
		engine.addListener(counter);
		engine.runSimulation(100L);
		assertEquals(10L, counter.getItemCount());
	}

	@Test
	public void generationCountTestWithGenerationAtZero() {
		FlowItemCounter counter = new FlowItemCounter(); 
		source.setCreateFirstAtTimeZero(true);
		engine.init(model);
		engine.addListener(counter);
		engine.runSimulation(100L);
		assertEquals(11L, counter.getItemCount());
	}

}
