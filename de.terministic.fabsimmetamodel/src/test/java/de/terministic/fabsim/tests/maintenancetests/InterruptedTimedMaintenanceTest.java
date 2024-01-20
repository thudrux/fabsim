package de.terministic.fabsim.tests.maintenancetests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.statistics.FinishedFlowItemCounter;

/*
 * test, if after a set period of time the machine worked actively, the maintenance for the machine starts. In this case,
 *  the simulation time is 65L, every 5L a new Item is released and the machine needs 2L to process 1 item. After 52L,
 *  the set maintenance begins, because 10 items have been processed with a processing time of 2L each.The maintenance runs for 10L.
 * After that, there is enough time for 1 more item before the simulation stops, resulting in 11 items in the sink.
 */

public class InterruptedTimedMaintenanceTest {

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

		IValue obj10 = model.getDurationObjectFactory().createConstantDurationObject(10L);
		IValue obj20 = model.getDurationObjectFactory().createConstantDurationObject(20L);
		model.getSimComponentFactory().createProcessTimeBasedMaintenanceAndAddToToolGroup("Maintenance1", obj10, obj20,
				toolGroup);// name, duration, processtime, toolGroup

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product, 5L);
		source.setLotSize(6);
		source.setAllowSplit(true);

		engine = new FabSimulationEngine();
	}

	@Test
	public void InterruptedTimedTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(65L);
		Assertions.assertEquals(11, counter.getItemCount());
	}

}
