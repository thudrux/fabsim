package de.terministic.fabsim.tests.dispatchingtests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.dispatchRules.FIFO;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * Test to see if the dispatching rule FIFO works as intended. Therefore we got 2 sources which both send their items to the same
 * machine. A new item from source1 is arriving every 5L, from source 2 every 3L. Processing time for a source1 item is 5L, for a source2
 * item is 2L. FIFO means the 1st item to get to the machine gets processed. With that in mind, the first item to get there is one from
 * source2. At 3L we start to process the item for 2L. At 5L a new item from source 1 arrives and is processed for 5L. At 10L not we got 
 * 3 items in queue, 2 from source2 and 1 from source1. The items from source2 arrived earlier so they should be processed 1st. With a 
 * simulation time of 15L, at the end both source2 items in queue should have been processed resulting in 4 items in the sink.
 */

public class FifoTest {
	
	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
	ToolGroup toolGroup;
	Sink sink;
	FIFO fifo;

	@Before
	public void setUp() throws Exception{
		model = new FabModel();	
		sink = (Sink)model.getSimComponentFactory().createSink();
		
		toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("Machine1", 1, ProcessingType.LOT);
		toolGroup.setDispatchRule(fifo);
		
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 5L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		
		Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, ProcessType.LOT, recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe2);
		Product product1=model.getSimComponentFactory().createProduct("Product1", recipe);

		source = (LotSource)model.getSimComponentFactory().createSource("Source1", product1, 5L);
		source.setLotSize(1);
		source.setAllowSplit(false);
		
		Product product2=model.getSimComponentFactory().createProduct("Product2", recipe2);

		source2 = (LotSource)model.getSimComponentFactory().createSource("Source2", product2, 3L);
		source2.setLotSize(1);
		source2.setAllowSplit(false);

		EventListManager eventList= new EventListManager();
		engine = new SimulationEngine(eventList);
	}
	

	@Test
	public void FifoTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter(); 
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(14L);
		assertEquals(4, counter.getItemCount());
	}

}
