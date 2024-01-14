package de.terministic.fabsim.tests.dedicationtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.Source;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.dedication.Dedication;
import de.terministic.fabsim.metamodel.components.equipment.dedication.DedicationDetails;
import de.terministic.fabsim.metamodel.statistics.FinishedFlowItemCounter;

public class BasicDedicationTest {

	FabModel model;
	SimulationEngine engine;
	Source source;
	ToolGroup toolGroup;
	ToolGroup toolGroup2;
	ProcessStep tgStep1;
	ProcessStep tgStep2;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup", 2);
		toolGroup.setConsidersDedication(true);
		Dedication dedication1 = new Dedication("Ded1");
		Dedication dedication2 = new Dedication("Ded2");
		((AbstractTool) toolGroup.getToolList().get(0)).getDedications().add(dedication1);
		((AbstractTool) toolGroup.getToolList().get(1)).getDedications().add(dedication2);

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		tgStep1 = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L,
				ProcessType.LOT, recipe);
		tgStep1.setDetails(new DedicationDetails(dedication1));
		tgStep2 = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", toolGroup, 3L,
				ProcessType.LOT, recipe);
		tgStep2.setDetails(new DedicationDetails(dedication2));
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (Source) model.getSimComponentFactory().createSource("Source1", product, 1L);

		engine = new FabSimulationEngine();
	}

	@Test
	public void dedicationTestWithTwoMachines() {
		FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		engine.init(model);
		engine.addListener(counter);
		engine.runSimulation(25L); // 25 runs, so 25 lots
		Assertions.assertEquals(7, counter.getItemCount());
	}
}
