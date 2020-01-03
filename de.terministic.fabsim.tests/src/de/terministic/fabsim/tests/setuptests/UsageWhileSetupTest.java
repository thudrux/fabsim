package de.terministic.fabsim.tests.setuptests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * test, if a machine can work while it's in a Setup To test this, we have 2 sources, each releasing an item with a different recipe.
 * One requires the machine to be in setup state 1, the other in setup state 2.
 * The simulation time is 26L, every 1L a new Item from source 1 is released, every 1L from source 2. The start Setup is at s1. After 10
 * items in queue, the Setup will change to s2. With a processing time of 5L for source1 items, 2 items will have been processed till then.
 * At 15L the Setup is complete(it takes 5L), With 2L for each item of source2 now, the machine can process 5 more items until the simulation
 * ends. That makes 12 Items in the end. If the machine worked while in Setup, it would be 13 and wrong.
 */


public class UsageWhileSetupTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
	ToolGroup toolGroup;
	Sink sink;

	@Before
	public void setUp() throws Exception{
		model = new FabModel();	
		sink = (Sink)model.getSimComponentFactory().createSink();
		
		toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup", 1, ProcessingType.LOT);
		SetupState s1 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s1", toolGroup);
		SetupState s2 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s2", toolGroup);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, 5L, toolGroup);
		
		AbstractSetupStrategy strategy = model.getSimComponentFactory().createBasicConditionBasedSetupStrategyAndAddToToolGroup(toolGroup);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s1, 30, strategy);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s2,10, strategy);

		toolGroup.setInitialSetup(s1);
		
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, s1, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		
		Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, s2, ProcessType.LOT, recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe2);
		
		Product product1=model.getSimComponentFactory().createProduct("Product1", recipe);
		source = (LotSource)model.getSimComponentFactory().createSource("Source1", product1, 1L);
		source.setLotSize(1);
		source.setAllowSplit(false);
		Product product2=model.getSimComponentFactory().createProduct("Product2", recipe2);
		source2 = (LotSource)model.getSimComponentFactory().createSource("Source2", product2, 1L);
		source2.setLotSize(1);
		source2.setAllowSplit(false);

		EventListManager eventList= new EventListManager();
		engine = new SimulationEngine(eventList);
	}
	

	@Test
	public void UsageWhileChangingTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter(); 
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(26L);
		assertEquals(7, counter.getItemCount());
	}

}
