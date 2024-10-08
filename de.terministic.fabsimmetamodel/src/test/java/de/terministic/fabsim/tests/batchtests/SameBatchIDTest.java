package de.terministic.fabsim.tests.batchtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.statistics.FinishedFlowItemCounter;

/*
 * Test to see if the batch recognizes different batch id's. Therefore 1 6 Item batch is worked on by machine 1 for 2L, afterwards by machine 2 for 3L
 * and finally by machine 1 again for 4L. If the machine applies and respects the id's correctly, 6 items are in the sink after a 20L long simulation.
 * If not, it should be quite a few more and false.
 */

public class SameBatchIDTest {

	FabModel model;
	SimulationEngine engine;
	LotSource source;
	ToolGroup toolGroup;
	Sink sink;

	@Test
	public void SameIDTest() {
		final FinishedFlowItemCounter counter = new FinishedFlowItemCounter();
		this.engine.init(this.model);
		this.sink.addListener(counter);
		this.engine.runSimulation(20L);
		Assertions.assertEquals(6, counter.getItemCount());
	}

	@BeforeEach
	public void setUp() throws Exception {
		this.model = new FabModel();
		this.sink = (Sink) this.model.getSimComponentFactory().createSink();

		this.toolGroup = (ToolGroup) this.model.getSimComponentFactory().createToolGroup("Toolgroup", 1,
				ProcessingType.BATCH);
		final BatchDetails typ1 = this.model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup("typ1", 6, 6,
				this.toolGroup);
		final BatchDetails typ2 = this.model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup("typ2", 3, 3,
				this.toolGroup);
		final BatchDetails typ3 = this.model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup("typ3", 6, 6,
				this.toolGroup);
		this.toolGroup.addBatchDetails(typ2);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, 2L, typ1,
				ProcessType.BATCH, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", this.toolGroup, 3L, typ2,
				ProcessType.BATCH, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step3", this.toolGroup, 4L, typ3,
				ProcessType.BATCH, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step4", this.sink, 0L, ProcessType.BATCH,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		this.source = (LotSource) this.model.getSimComponentFactory().createSource("Source1", product, 1L);
		this.source.setLotSize(1);
		this.source.setAllowSplit(true);

		this.engine = new FabSimulationEngine();
	}

}