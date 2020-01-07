package de.terministic.fabsim.tests.maintenancetests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.AbstractDurationObject;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * test, if a machine keeps working while in maintenance. In this case, the simulation time is 26L, every 1L a new Item is released
 * and the machine needs 2L to process 1 item. After 20L there is a set maintenance for 3L, in which no itmes should be processed. 
 * If in that time no items are processed, 11 items will have been finished once the simulation completes.
 */

public class WorkingWhileMaintenanceTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	ToolGroup toolGroup;
	Sink sink;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		sink = (Sink) model.getSimComponentFactory().createSink();
		toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup", 1, ProcessingType.LOT);
		AbstractDurationObject obj3 = model.getDurationObjectFactory().createConstantDurationObject(3L);
		AbstractDurationObject obj20 = model.getDurationObjectFactory().createConstantDurationObject(20L);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup("Maintenance1", obj3,
				obj20, toolGroup);// name, duration, simtime, toolGroup

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product, 1L);
		source.setLotSize(6);
		source.setAllowSplit(true);

		EventListManager eventList = new EventListManager();
		engine = new SimulationEngine(eventList);
	}

	@Test
	public void WorkingWhileMaintenanceTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(26L);
		Assertions.assertEquals(11, counter.getItemCount());
	}

}
