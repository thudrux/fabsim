package de.terministic.fabsim.tests.setuptests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.metamodel.statistics.FinishedFlowItemCounter;

/*
 * test, if the SetupChange works as intended. To test this, we have 2 sources, each releasing an item with a different recipe. 1 requires the machine to be in setup state 1, the other in setup state 2.
 * The simulation time is 16L, every 1L a new Item from source 1 is released, every 1L from source 2, the start setup is s2. The machine is set to change the Setup after 10L of processing time, which will be
 * after 5 items due to the 2L processing time for each item. Then the 5L long SetupChange starts. With a 16L long simulation no more items
 * should be processed anymore, meaning 5 items must be in the sink.
 */

public class TimedSetupFunctionTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
	ToolGroup toolGroup;
	Sink sink;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		sink = (Sink) model.getSimComponentFactory().createSink();

		toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup", 1, ProcessingType.LOT);
		SetupState s1 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s1", toolGroup);
		SetupState s2 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s2", toolGroup);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s2, s1, 5L, toolGroup);
		AbstractSetupStrategy strategy = model.getSimComponentFactory()
				.createBasicConditionBasedSetupStrategyAndAddToToolGroup(toolGroup);
		model.getSimComponentFactory().createMinProductiveTimeSinceSetupChangeConditionAndAddToSetupStrategy(s1, 5L,
				strategy);
		model.getSimComponentFactory().createMinProductiveTimeSinceSetupChangeConditionAndAddToSetupStrategy(s2, 10L,
				strategy);

		toolGroup.setInitialSetup(s2);

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, s1, ProcessType.LOT,
				recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);

		Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, s2, ProcessType.LOT,
				recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe2);

		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe);
		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product1, 2L);
		source.setLotSize(1);
		source.setAllowSplit(false);

		Product product2 = model.getSimComponentFactory().createProduct("Product2", recipe2);
		source2 = (LotSource) model.getSimComponentFactory().createSource("Source2", product2, 1L);
		source2.setLotSize(1);
		source2.setAllowSplit(false);

		engine = new FabSimulationEngine();
	}

	@Test
	public void TimedFunctionTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(16L);
		Assertions.assertEquals(5, counter.getItemCount());
	}
}
