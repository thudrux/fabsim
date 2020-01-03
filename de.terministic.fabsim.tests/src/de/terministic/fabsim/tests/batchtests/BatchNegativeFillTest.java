package de.terministic.fabsim.tests.batchtests;

import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.Controller;
import de.terministic.fabsim.components.ToolGroupController;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.toolstatemachine.BasicToolStateMachine;
import de.terministic.fabsim.core.FabModel;

//Test, if the Batch can be filled with a negative number.

public class BatchNegativeFillTest {

	ToolGroup toolGroup;

	@Test(expected = IllegalArgumentException.class)
	public void NegativeFillTest() {
		final FabModel model = new FabModel();
		final Controller controller = new Controller(model, null, null);
		final ToolGroupController tgController = new ToolGroupController(model, controller);

		this.toolGroup = new ToolGroup(model,"Batch1", ProcessingType.BATCH, 1, new BasicToolStateMachine(model), tgController,
				null);

		final BatchDetails typ1 = new BatchDetails("Typ1", -5, -20);// (Name,
																	// minBatch,
																	// maxBatch)
		this.toolGroup.addBatchDetails(typ1);// (Name, minBatch, maxBatch)
	}

}