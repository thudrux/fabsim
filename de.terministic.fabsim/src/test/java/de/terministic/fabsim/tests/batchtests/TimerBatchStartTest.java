package de.terministic.fabsim.tests.batchtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

/*
 * Test to see if the batch starts when it's slowly filled and doesn't reach the minBatch threshold, but a timer is set to let it go.
 * Therefore the Simulation runs 26L, with 1 new Item getting into the batch every 6L. The minBatch is 5, meaning it will not be reached in the time
 * the simulation, but with a timer of 15L(which starts after the 1st item reached the batch), it should start at 21L. If 3 Items are in the sink,
 * after the simulation stopped, the test was successful.
 */

public class TimerBatchStartTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	ToolGroup toolGroup;
	Sink sink;

	@BeforeEach
	public void setUp() throws Exception {
		this.model = new FabModel();
		this.sink = (Sink) this.model.getSimComponentFactory().createSink();

		this.toolGroup = (ToolGroup) this.model.getSimComponentFactory().createToolGroup("Toolgroup", 1,
				ProcessingType.BATCH);
		final BatchDetails typ1 = this.model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup("typ1", 5, 20,
				15L, this.toolGroup);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, 5L, typ1,
				ProcessType.BATCH, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", this.sink, 0L, ProcessType.BATCH,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		this.source = (LotSource) this.model.getSimComponentFactory().createSource("Source1", product, 6L);
		this.source.setLotSize(1);
		this.source.setAllowSplit(true);

		final EventListManager eventList = new EventListManager();
		this.engine = new SimulationEngine(eventList);
	}

	@Test
	public void TimedStartTest() {
		final FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		this.engine.init(this.model);
		this.sink.addListener(counter);
		this.engine.runSimulation(27L);
		Assertions.assertEquals(3, counter.getItemCount());
	}

}