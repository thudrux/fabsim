package de.terministic.fabsim.tests.modeltests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.Source;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.statistics.FlowItemCounter;

public class SourceSinkModelTest {

	FabModel model;
	SimulationEngine engine;
	Source source;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (Source) model.getSimComponentFactory().createSource("Source1", product, 10L);

		engine = new FabSimulationEngine();
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void generationCountTestDefault() {
		FlowItemCounter counter = new FlowItemCounter();
		engine.init(model);
		engine.addListener(counter);
		engine.runSimulation(100L);
		Assertions.assertEquals(10L, counter.getItemCount());
	}

	@Test
	public void generationCountTestWithGenerationAtZero() {
		FlowItemCounter counter = new FlowItemCounter();
		source.setCreateFirstAtTimeZero(true);
		engine.init(model);
		engine.addListener(counter);
		engine.runSimulation(100L);
		Assertions.assertEquals(11L, counter.getItemCount());
	}

}
