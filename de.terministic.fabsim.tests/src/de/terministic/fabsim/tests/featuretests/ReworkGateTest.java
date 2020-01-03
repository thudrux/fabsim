package de.terministic.fabsim.tests.featuretests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.ReworkDetails;
import de.terministic.fabsim.components.ReworkGate;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.AbstractSink;
import de.terministic.fabsim.core.AbstractSource;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FabKPIOverview;
import de.terministic.fabsim.statistics.FlowItemTracker;
import de.terministic.fabsim.statistics.SimulationResultAggregator;

public class ReworkGateTest {

	@Test
	public void ZeroReworkTest() {
		
		FabModel model = new FabModel();
		
		AbstractSink sink = model.getSimComponentFactory().createSink();
		ReworkGate gate = model.getSimComponentFactory().createReworkGate("ReworkGate1");

		ToolGroup tg1 = (ToolGroup)model.getSimComponentFactory().createToolGroup("ToolGroup1", 1);
		
		Recipe recipe1 = model.getSimComponentFactory().createRecipe("Recipe1");
		
		ReworkDetails details = new ReworkDetails(0.0, 0);
		
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("ProcessStep", tg1, 3L, ProcessType.LOT, recipe1);		
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Test", gate,0L, ProcessType.LOT, recipe1, details);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("SinkStep", sink, 0L, ProcessType.LOT, recipe1);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe1);

		AbstractSource source = model.getSimComponentFactory().createSource("Source", product, 4L);
		
		SimulationEngine engine = new SimulationEngine(new EventListManager());
		engine.init(model);
		SimulationResultAggregator sra = new SimulationResultAggregator();
		engine.addListeners(sra.neededListeners());
		engine.runSimulation(100L);
		FabKPIOverview results = sra.generateResults(0L, 100L);
		assertEquals(24L, results.getThroughput());
		assertEquals(3.0, results.getAvgCycleTime(),0.1);
	}

	@Test
	public void tenPercentReworkTest() {
		
		FabModel model = new FabModel();
		
		AbstractSink sink = model.getSimComponentFactory().createSink();
		ReworkGate gate = model.getSimComponentFactory().createReworkGate("ReworkGate1");

		ToolGroup tg1 = (ToolGroup)model.getSimComponentFactory().createToolGroup("ToolGroup1", 1);
		
		Recipe recipe1 = model.getSimComponentFactory().createRecipe("Recipe1");
		
		ReworkDetails details = new ReworkDetails(0.1, 0);
		
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("ProcessStep", tg1, 3L, ProcessType.LOT, recipe1);		
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Test", gate,0L, ProcessType.LOT, recipe1, details);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("SinkStep", sink, 0L, ProcessType.LOT, recipe1);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe1);
		
		LotSource source =(LotSource) model.getSimComponentFactory().createSource("Source", product, 35L);
		source.setCreateFirstAtTimeZero(true);
		
		SimulationEngine engine = new SimulationEngine(new EventListManager());
		engine.init(model);
		SimulationResultAggregator sra = new SimulationResultAggregator();
		engine.addListeners(sra.neededListeners());
		engine.runSimulation(10000000L);

		FabKPIOverview results = sra.generateResults(0L, 100L);
		assertEquals(3.3, results.getAvgCycleTime(),0.05);
	}

}
