package de.terministic.fabsim.tests.statistics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.Source;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.statistics.FabKPIOverview;
import de.terministic.fabsim.metamodel.statistics.SimulationResultAggregator;

public class SimulationResultAggregatorTest {

	@Test
	public void emptyRecipeSimResultAggregatorTest() {
		FabModel model = new FabModel();
		SimulationEngine engine = new SimulationEngine();

		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT, recipe);
		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe);
		Source source = (Source) model.getSimComponentFactory().createSource("Source1", product1, 8L);

		SimulationResultAggregator agg = new SimulationResultAggregator();

		engine.init(model);
		engine.addListeners(agg.neededListeners());
		long simEnd = 50L;
		engine.runSimulation(simEnd);

		FabKPIOverview overview = agg.generateResults(0L, simEnd);

		Assertions.assertEquals(0.0, overview.getAvgCycleTime(), 0.001);
		Assertions.assertEquals(0.0, overview.getAvgWIP(), 0.001);
		Assertions.assertEquals(0.0, overview.getCycleTimeVariance(), 0.001);
		Assertions.assertEquals(1, overview.getMaxWIP());
		Assertions.assertEquals(0.0, overview.getFlowFactor(), 0.001);
	}

	@Test
	public void simpleRecipeSimResultAggregatorTest() {

		FabModel model = new FabModel();
		SimulationEngine engine = new SimulationEngine();

		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		AbstractToolGroup toolGroup = model.getSimComponentFactory().createToolGroup("ToolGroup1", 1);
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 4L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT, recipe);
		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe);
		Source source = (Source) model.getSimComponentFactory().createSource("Source1", product1, 8L);
		source.setCreateFirstAtTimeZero(true);

		SimulationResultAggregator agg = new SimulationResultAggregator();
		engine.addListeners(agg.neededListeners());

		engine.init(model);
		long simEnd = 8L;
		engine.runSimulation(simEnd);

		FabKPIOverview overview = agg.generateResults(0L, simEnd);

		Assertions.assertEquals(4.0, overview.getAvgCycleTime(), 0.001);
		Assertions.assertEquals(0.5, overview.getAvgWIP(), 0.001);
		// assertEquals(0.0, overview.getCycleTimeVariance(),0.001);
		Assertions.assertEquals(1, overview.getMaxWIP());
		Assertions.assertEquals(1.0, overview.getFlowFactor(), 0.001);
	}

}
