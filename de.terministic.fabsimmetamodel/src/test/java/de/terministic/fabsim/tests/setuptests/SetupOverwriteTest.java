package de.terministic.fabsim.tests.setuptests;

import java.util.List;

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
import de.terministic.fabsim.metamodel.statistics.ToolStateChangeLog;
import de.terministic.fabsim.metamodel.statistics.ToolStateLogEntry;

/*
 * test, if a Setup can start while the machine is always doing another Setup. To test this, we have 2 sources, each releasing an item with a different recipe.
 * One requires the machine to be in setup state 1, the other in setup state 2.
 * The simulation time is 6L, every 3L a new Item from source 1 is released, every 1L from source 2. Each Setup will be triggered with only
 * 1 Item in queue. The start Setup is at s1. After 1L, with 1 source2 Item in queue the Setup change to s2 will start, taking 5L. After 
 * 3L however, an Item from source 1 is in queue, which means the threshold for a change to s1 is already reached. It shouldn't stop the
 * change to s2 though, so all we need to check is, if the SetupState of the machine at 6L is s2.
 */

public class SetupOverwriteTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
	ToolGroup toolGroup;
	SetupState s2;
	Sink sink;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		sink = (Sink) model.getSimComponentFactory().createSink();

		toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup", 1, ProcessingType.LOT);
		SetupState s1 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s1", toolGroup);
		s2 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s2", toolGroup);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, 5L, toolGroup);

		AbstractSetupStrategy strategy = model.getSimComponentFactory()
				.createBasicConditionBasedSetupStrategyAndAddToToolGroup(toolGroup);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s1, 1, strategy);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s2, 1, strategy);

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

		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product1, 3L);
		source.setLotSize(1);
		source.setAllowSplit(false);
		Product product2 = model.getSimComponentFactory().createProduct("Product2", recipe2);

		source2 = (LotSource) model.getSimComponentFactory().createSource("Source2", product2, 1L);
		source2.setLotSize(1);
		source2.setAllowSplit(false);

		engine = new FabSimulationEngine();
	}

	@Test
	public void OverwriteTest() {
		ToolStateChangeLog Log = new ToolStateChangeLog();
		engine.init(model);
		toolGroup.addListener(Log);
		engine.runSimulation(6L);
		List<ToolStateLogEntry> toolLogList = Log.getLog().get(toolGroup.getToolByIndex(0));
		Assertions.assertEquals(s2, toolLogList.get(2).getSetupState());
	}

}
