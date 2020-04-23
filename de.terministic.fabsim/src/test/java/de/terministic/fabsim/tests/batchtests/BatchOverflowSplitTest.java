package de.terministic.fabsim.tests.batchtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * Test, if the Batch can be filled with more items than it's maximum capacity. Therefore the tests runs 16L. At 10L 2 Lots have been released,
 * 6 items each. In this case, the splitting of the Lots is allowed, allowing for the Batch to get it's maximum capacity of 10 filled and to start. With
 * a production Time of 5L, there should be 10 items at the sink after the simulation stopped. If both Lots were filled completely into the batch it would
 * be 12 items and the test failed.
 */

public class BatchOverflowSplitTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	ToolGroup toolGroup;
	Sink sink;

	@Tag("fast")
	@Test
	public void OverflowSplitTest() {
		final FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		this.engine.init(this.model);
		this.sink.addListener(counter);
		this.engine.runSimulation(16L);
		Assertions.assertEquals(1, counter.getItemCount());
	}

	@BeforeEach
	public void setUp() throws Exception {
		this.model = new FabModel();
		this.sink = (Sink) this.model.getSimComponentFactory().createSink();
		this.toolGroup = (ToolGroup) this.model.getSimComponentFactory().createToolGroup("Toolgroup", 1,
				ProcessingType.BATCH);
		final BatchDetails typ1 = this.model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup("typ1", 5, 5,
				this.toolGroup);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, 5L, typ1,
				ProcessType.BATCH, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", this.sink, 0L, ProcessType.LOT,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		this.source = (LotSource) this.model.getSimComponentFactory().createSource("Source1", product, 5L);
		this.source.setLotSize(6);
		this.source.setAllowSplit(true);

		this.engine = new SimulationEngine();
	}

}