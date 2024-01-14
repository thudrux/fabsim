package de.terministic.fabsim.tests.featuretests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.AbstractSink;
import de.terministic.fabsim.metamodel.AbstractSource;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.ReworkDetails;
import de.terministic.fabsim.metamodel.components.ReworkGate;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.statistics.FabKPIOverview;
import de.terministic.fabsim.metamodel.statistics.SimulationResultAggregator;

public class ReworkGateTest {

	@Test
	public void ZeroReworkTest() {

		FabModel model = new FabModel();

		AbstractSink sink = model.getSimComponentFactory().createSink();
		ReworkGate gate = model.getSimComponentFactory().createReworkGate("ReworkGate1");

		ToolGroup tg1 = (ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup1", 1);

		Recipe recipe1 = model.getSimComponentFactory().createRecipe("Recipe1");

		ReworkDetails details = new ReworkDetails(0.0, 0);

		model.getSimComponentFactory().createProcessStepAndAddToRecipe("ProcessStep", tg1, 3L, ProcessType.LOT,
				recipe1);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Test", gate, 0L, ProcessType.LOT, recipe1,
				details);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("SinkStep", sink, 0L, ProcessType.LOT, recipe1);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe1);

		AbstractSource source = model.getSimComponentFactory().createSource("Source", product, 4L);

		SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		SimulationResultAggregator sra = new SimulationResultAggregator();
		engine.addListeners(sra.neededListeners());
		engine.runSimulation(100L);
		FabKPIOverview results = sra.generateResults(0L, 100L);
		Assertions.assertEquals(24L, results.getThroughput());
		Assertions.assertEquals(3.0, results.getAvgCycleTime(), 0.1);
	}

//	@Test
	public void tenPercentReworkTest() {

		FabModel model = new FabModel();

		AbstractSink sink = model.getSimComponentFactory().createSink();
		ReworkGate gate = model.getSimComponentFactory().createReworkGate("ReworkGate1");

		ToolGroup tg1 = (ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup1", 1);

		Recipe recipe1 = model.getSimComponentFactory().createRecipe("Recipe1");

		ReworkDetails details = new ReworkDetails(0.1, 0);

		model.getSimComponentFactory().createProcessStepAndAddToRecipe("ProcessStep", tg1, 3L, ProcessType.LOT,
				recipe1);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Test", gate, 0L, ProcessType.LOT, recipe1,
				details);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("SinkStep", sink, 0L, ProcessType.LOT, recipe1);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe1);

		LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source", product, 35L);
		source.setCreateFirstAtTimeZero(true);

		SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		SimulationResultAggregator sra = new SimulationResultAggregator();
		engine.addListeners(sra.neededListeners());
		engine.runSimulation(10000000L);

		FabKPIOverview results = sra.generateResults(0L, 100L);
		Assertions.assertEquals(3.3, results.getAvgCycleTime(), 0.05);
	}

}
