package de.terministic.fabsim.tests.batchtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.Controller;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.AbstractToolGroupController;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.ToolGroupController;
import de.terministic.fabsim.components.equipment.toolstatemachine.BasicToolStateMachine;
import de.terministic.fabsim.core.FabModel;

//Test, if the Batch can be filled with a negative number.
class BatchNegativeFillTest {

	ToolGroup toolGroup;

	@Tag("fast")
	@Test
	void NegativeFillTest() {
		final FabModel model = new FabModel();
		final Controller controller = new Controller(model, null, null);
		final AbstractToolGroupController tgController = new ToolGroupController(model, controller);

		this.toolGroup = new ToolGroup(model, "Batch1", ProcessingType.BATCH, 1, new BasicToolStateMachine(model),
				tgController, null);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final BatchDetails typ1 = new BatchDetails("Typ1", -5, -20);// (Name, minBatch, maxBatch)
		});
	}

}