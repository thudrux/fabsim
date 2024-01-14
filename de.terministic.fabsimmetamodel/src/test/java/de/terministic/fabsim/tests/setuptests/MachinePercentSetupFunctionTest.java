package de.terministic.fabsim.tests.setuptests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.statistics.FinishedFlowItemCounter;

/*
 * test, if machines change their Setup automatically to fit a given distribution. To test this, we got 2 similar machines. Both got
 * the same SetupStates, SetupChanges and recipes they can work with. The distribution is set to 1,1 meaning the same amounts of machines
 * should work on both setups. Both machines start at s1, meaning at the beginning 1 of them should change their state. The other 
 * should start working on the product already, that takes 5L to process. After the 5L long SetupChange, the other machine works on
 * the other product with its new state s2, that needs 2L to process. With 12L simulation 5 items should be in the sink at the end if
 * everything worked as intended.
 */

public class MachinePercentSetupFunctionTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	LotSource source2;
	LotSource source3;
	ToolGroup toolGroup;
	ToolGroup toolGroup2;
	Sink sink;

	@BeforeEach
	public void setUp() throws Exception {
		/*
		 * model = new FabModel(new BasicRouting()); sink =
		 * (Sink)model.getSimComponentFactory().createSink();
		 * 
		 * toolGroup=(ToolGroup)
		 * model.getSimComponentFactory().createToolGroup("Machine1", 2,
		 * ProcessingType.LOT); SetupState s1 =
		 * model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s1",
		 * toolGroup); SetupState s2 =
		 * model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s2",
		 * toolGroup);
		 * model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, 5L,
		 * true, toolGroup); AbstractSetupStrategy strategy =
		 * model.getSimComponentFactory().
		 * createBasicConditionBasedSetupStrategyAndAddToToolGroup(toolGroup);
		 * model.getSimComponentFactory().
		 * createMinNumberOfToolsSetupChangeConditionAndAddToSetupStrategy(s1, 1,
		 * strategy); model.getSimComponentFactory().
		 * createMinNumberOfToolsSetupChangeConditionAndAddToSetupStrategy(s2, 1,
		 * strategy);
		 * 
		 * toolGroup.getToolByIndex(0).setInitialSetupState(s1);
		 * toolGroup.getToolByIndex(1).setInitialSetupState(s2);
		 * 
		 * Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		 * model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1",
		 * toolGroup, 5L, s1, recipe);
		 * model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink,
		 * 0L, recipe);
		 * 
		 * Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		 * model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1",
		 * toolGroup, 2L, s2, recipe2);
		 * model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink,
		 * 0L, recipe2);
		 * 
		 * Recipe recipe3 = model.getSimComponentFactory().createRecipe("Recipe3");
		 * model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1",
		 * toolGroup, 5L, s1, recipe3);
		 * model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink,
		 * 0L, recipe3);
		 * 
		 * source = (LotSource)model.getSimComponentFactory().createSource("Source1",
		 * recipe, 1L); source.setLotSize(1); source.setAllowSplit(false); source2 =
		 * (LotSource)model.getSimComponentFactory().createSource("Source2", recipe2,
		 * 1L); source2.setLotSize(1); source2.setAllowSplit(false); source3 =
		 * (LotSource)model.getSimComponentFactory().createSource("Source3", recipe3,
		 * 2L); source3.setLotSize(1); source3.setAllowSplit(false);
		 * 
		 * TimeGroupedEventListManager eventList= new TimeGroupedEventListManager(); engine = new
		 * SimulationEngine(eventList);
		 */ }

	// TODO implement Functionality
	public void MachinePercentageTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(12L);
		Assertions.assertEquals(5, counter.getItemCount());
	}
}
