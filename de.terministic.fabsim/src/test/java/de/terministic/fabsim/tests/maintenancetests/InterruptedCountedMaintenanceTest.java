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
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.AbstractDurationObject;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * test, if after a set number of items the maintenance for the machine starts with the machine not constantly working.
 * In this case, the simulation time is 65L, every 5L a new Item is released and the machine needs 2L to process 1 item. 
 * That means, that after 52L the threshold for the 10 items is reached, now starts a 10L long maintenance.
 * After that, there is enough time for 1 more item before the simulation stops, resulting in 11 items in the sink.
 */

public class InterruptedCountedMaintenanceTest {

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
		AbstractDurationObject obj10 = model.getDurationObjectFactory().createConstantDurationObject(10L);
		model.getSimComponentFactory().createItemBasedMaintenanceAndAddToToolGroup("Maintenance1", obj10, 10,
				toolGroup);

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product, 5L);
		source.setLotSize(6);
		source.setAllowSplit(true);

		engine = new SimulationEngine();
	}

	@Test
	public void InterruptedCountedTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(65L);
		Assertions.assertEquals(11, counter.getItemCount());
	}

}
