package de.terministic.fabsim.tests.batchtests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * Test to see if the batch starts when it reached the maxBatch threshold. Therefore the Simulation runs 25L, with 1 new Item
 * getting into the batch every 5L. The minBatch is 3, meaning it will be reached after 15L. With 5L production time, there should be 3 Items in the
 * sink after the simulation stopped.
 */

public class fullBatchStartTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	ToolGroup toolGroup;
	Sink sink;

	@Test
	public void FullStartTest() {
		final FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		this.engine.init(this.model);
		this.sink.addListener(counter);
		this.engine.runSimulation(25L);
		assertEquals(3, counter.getItemCount());
	}

	@Before
	public void setUp() throws Exception {
		this.model = new FabModel();
		this.sink = (Sink) this.model.getSimComponentFactory().createSink();
		this.toolGroup = (ToolGroup) this.model.getSimComponentFactory().createToolGroup("Toolgroup_Batch1", 1,
				ProcessingType.BATCH);
		final BatchDetails typ1 = this.model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup("typ1", 3, 3,
				this.toolGroup);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, 5L, typ1,ProcessType.BATCH, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", this.sink, 0L,ProcessType.BATCH, recipe);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);

		this.source = (LotSource) this.model.getSimComponentFactory().createSource("Source1", product, 5L);
		this.source.setLotSize(1);
		this.source.setAllowSplit(false);

		final EventListManager eventList = new EventListManager();
		this.engine = new SimulationEngine(eventList);
	}

}