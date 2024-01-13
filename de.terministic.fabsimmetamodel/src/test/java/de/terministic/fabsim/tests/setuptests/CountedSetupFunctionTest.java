package de.terministic.fabsim.tests.setuptests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.core.FabModel;
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
 * The simulation time is 31L, every 1L a new Item from source 1 is released, every 2L from source 2. The machine is set to change the Setup after 5 processed Items, which will be
 * after 25L due to the 5L processing time for each item. Then the 5L long SetupChange starts. With a 31L long simulation no more items
 * should be processed anymore, meaning 5 items must be in the sink.
 */

public class CountedSetupFunctionTest {

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
		AbstractSetupStrategy strategy = model.getSimComponentFactory()
				.createBasicConditionBasedSetupStrategyAndAddToToolGroup(toolGroup);
		model.getSimComponentFactory().createMinProducedItemsSinceChangeSetupChangeConditionAndAddToSetupStrategy(s1, 5,
				strategy);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, 5L, toolGroup);
		toolGroup.setInitialSetup(s1);

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, s1, ProcessType.LOT,
				recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);

		Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 3L, s2, ProcessType.LOT,
				recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe2);
		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe);
		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product1, 1L);
		source.setLotSize(1);
		source.setAllowSplit(false);
		Product product2 = model.getSimComponentFactory().createProduct("Product2", recipe2);
		source2 = (LotSource) model.getSimComponentFactory().createSource("Source2", product2, 2L);
		source.setLotSize(1);
		source.setAllowSplit(false);

		engine = new SimulationEngine();
	}

	@Test
	public void CountedFunctionTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(31L);
		Assertions.assertEquals(5, counter.getItemCount());
	}
}
