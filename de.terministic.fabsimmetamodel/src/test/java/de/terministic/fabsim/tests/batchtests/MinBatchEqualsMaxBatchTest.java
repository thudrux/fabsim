package de.terministic.fabsim.tests.batchtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.components.Controller;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroupController;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroupController;
import de.terministic.fabsim.metamodel.components.equipment.toolstatemachine.BasicToolStateMachine;
import de.terministic.fabsim.metamodel.FabModel;

//Test, if you can set either minBatch or maxBatch "0"

public class MinBatchEqualsMaxBatchTest {

	ToolGroup toolGroup;

	@Test
	public void MaxBatchZeroTest() {
		final FabModel model = new FabModel();
		final Controller controller = new Controller(model, null, null);
		final AbstractToolGroupController tgController = new ToolGroupController(model, controller);

		this.toolGroup = new ToolGroup(model, "Batch1", ProcessingType.BATCH, 1, new BasicToolStateMachine(model),
				tgController, null);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			this.toolGroup.addBatchDetails(new BatchDetails("typ1", 5, 0));// (Name,
																			// minBatch,
																			// maxBatch)

		});
	}

	@Test
	public void MinBatchEqualsMaxBatchTest() {
		final FabModel model = new FabModel();
		final Controller controller = new Controller(model, null, null);
		final AbstractToolGroupController tgController = new ToolGroupController(model, controller);

		this.toolGroup = new ToolGroup(model, "Batch1", ProcessingType.BATCH, 1, new BasicToolStateMachine(model),
				tgController, null);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			this.toolGroup.addBatchDetails(new BatchDetails("typ1", 0, 0));// (Name,
																			// minBatch,
																			// maxBatch)

		});
	}

	@Test
	public void MinBatchZeroTest() {
		final FabModel model = new FabModel();
		final Controller controller = new Controller(model, null, null);
		final AbstractToolGroupController tgController = new ToolGroupController(model, controller);

		this.toolGroup = new ToolGroup(model, "Batch1", ProcessingType.BATCH, 1, new BasicToolStateMachine(model),
				tgController, null);
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			this.toolGroup.addBatchDetails(new BatchDetails("typ1", 0, 5));// (Name,
																			// minBatch,
																			// maxBatch)

		});
	}

}