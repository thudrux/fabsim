package de.terministic.fabsim.tests.setuptests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.ToolStateChangeLog;
import de.terministic.fabsim.statistics.ToolStateLogEntry;

/*
 * test, if a Setup can start while the machine is processing an item. To test this, we have 2 sources, each releasing an item with a different recipe.
 * One requires the machine to be in setup state 1, the other in setup state 2.
 * The simulation time is 10L, every 1L a new Item from source 1 is released, every 2L from source 2. The processing time for the item from
 * source1 is 10L, meaning the machine is working on 1 item for the entire simulation. With 2 items in queue from source2 a Setup will be
 * triggered. That happens after 4L. The SetupTime is 5L, meaning if the Setup would take place while the machine is processing an item
 * it would be in state s2 at 9L. So if we check at 10L that the state is still s1, we know everything went as intended.
 */


public class SetupWhileWorkingTest {
	
	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
	ToolGroup toolGroup;
	SetupState s1;
	Sink sink;

	@Before
	public void setUp() throws Exception{
		model = new FabModel();	
		sink = (Sink)model.getSimComponentFactory().createSink();
		
		toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup", 1, ProcessingType.LOT);
		s1 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s1", toolGroup);
		SetupState s2 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s2", toolGroup);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, 5L, toolGroup);
		
		AbstractSetupStrategy strategy = model.getSimComponentFactory().createBasicConditionBasedSetupStrategyAndAddToToolGroup(toolGroup);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s1, 10, strategy);
		model.getSimComponentFactory().createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(s2,2, strategy);

		toolGroup.setInitialSetup(s1);
				
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 10L, s1, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		
		Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, s2, ProcessType.LOT, recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe2);
		Product product1=model.getSimComponentFactory().createProduct("Product1", recipe);

		source = (LotSource)model.getSimComponentFactory().createSource("Source1", product1, 1L);
		source.setLotSize(1);
		source.setAllowSplit(false);
		Product product2=model.getSimComponentFactory().createProduct("Product1", recipe2);

		source2 = (LotSource)model.getSimComponentFactory().createSource("Source2", product2, 2L);
		source2.setLotSize(1);
		source2.setAllowSplit(false);

		EventListManager eventList= new EventListManager();
		engine = new SimulationEngine(eventList);
	}
		
	@Test
	public void WhileWorkingTest() {
		ToolStateChangeLog Log = new ToolStateChangeLog(); 
		engine.init(model);
		toolGroup.addListener(Log);
		engine.runSimulation(10L);
		List<ToolStateLogEntry> toolLogList =Log.getLog().get(toolGroup.getToolByIndex(0));
		assertEquals(s1,toolLogList.get(toolLogList.size()-1).getSetupState());
	}

}