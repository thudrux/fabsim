package de.terministic.fabsim.tests.setuptests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.core.TimeGroupedEventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * test, if the SetupChange works as intended. To test this, we have 2 sources, each releasing an item with a different recipe. 1 requires the machine to be in setup state 1, the other in setup state 2.
 * The simulation time is 25L, every 2L a new Item from source 1 is released, every 1L from source 2. After 10 Items from source 2 in queue the machine
 * will change the state, which will take 5L. With 5L processing time for each source 1 Item, only 2 of these can be produced before the machine will
 * change the Setup. Once that's done, there is time for 4 source 2 Items to be processed(they need 2L), concluding in 6 items.
 */

public class SetupFunctionTest {

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
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, 5L, toolGroup);
		AbstractSetupStrategy strategy = model.getSimComponentFactory()
				.createBasicConditionBasedSetupStrategyAndAddToToolGroup(toolGroup);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s1, 20, strategy);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s2, 10, strategy);

		toolGroup.setInitialSetup(s1);

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

		TimeGroupedEventListManager eventList = new TimeGroupedEventListManager();
		engine = new SimulationEngine(eventList);
	}

	@Test
	public void FunctionTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(25L);
		Assertions.assertEquals(6, counter.getItemCount());
	}

}