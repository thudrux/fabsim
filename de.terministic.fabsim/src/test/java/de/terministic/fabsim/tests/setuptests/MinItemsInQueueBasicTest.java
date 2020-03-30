package de.terministic.fabsim.tests.setuptests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.core.AbstractSink;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;

public class MinItemsInQueueBasicTest {
	FabModel model;
	SimulationEngine engine;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		AbstractToolGroup toolGroup = model.getSimComponentFactory().createToolGroup("ToolGroup", 1,
				ProcessingType.LOT);
		SetupState s1 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("State1",
				(ToolGroup) toolGroup);
		SetupState s2 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("State2",
				(ToolGroup) toolGroup);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, 1L,
				(AbstractHomogeneousResourceGroup) toolGroup);
		AbstractSetupStrategy strategy = model.getSimComponentFactory()
				.createBasicConditionBasedSetupStrategyAndAddToToolGroup((AbstractHomogeneousResourceGroup) toolGroup);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s1, 3, strategy);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s2, 3, strategy);
		((AbstractHomogeneousResourceGroup) toolGroup).setInitialSetup(s1);

		AbstractSink sink = model.getSimComponentFactory().createSink("Sink");

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 1L, s1, ProcessType.LOT,
				recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("SinkStep", sink, 0L, ProcessType.LOT, recipe);

		Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 1L, s2, ProcessType.LOT,
				recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("SinkStep", sink, 0L, ProcessType.LOT, recipe2);
		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe);
		Product product2 = model.getSimComponentFactory().createProduct("Product2", recipe2);
		model.getSimComponentFactory().createSource("Source1", product1, 7L);
		model.getSimComponentFactory().createSource("Source2", product2, 5L);

		engine = new SimulationEngine();

	}

	@Test
	public void test() {
		engine.init(model);
		engine.runSimulation(50L);
		// TODO implement functionality
	}

}
