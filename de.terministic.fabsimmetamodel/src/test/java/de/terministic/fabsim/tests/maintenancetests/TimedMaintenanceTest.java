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
 * test, if after a set time period the maintenance for the machine starts. In this case, the simulation time is 26L, every 1L a new Item is released
 * and the machine needs 2L to process 1 item. After 20L, the set maintenance begins and runs for 3L.
 * After that, there is enough time for 1 more item before the simulation stops, resulting in 11 items in the sink.
 */

public class TimedMaintenanceTest {

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
		IValue obj3 = model.getValueObjectFactory().createConstantValueObject(3L);
		IValue obj20 = model.getValueObjectFactory().createConstantValueObject(20L);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup("Maintenance1", obj3,
				obj20, toolGroup);// name, duration, simtime, toolGroup

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product, 1L);
		source.setLotSize(6);
		source.setAllowSplit(true);

		engine = new FabSimulationEngine();
	}

	@Test
	public void TimedTest() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		sink.addListener(counter);
		engine.runSimulation(26L);
		Assertions.assertEquals(11, counter.getItemCount());
	}

}
