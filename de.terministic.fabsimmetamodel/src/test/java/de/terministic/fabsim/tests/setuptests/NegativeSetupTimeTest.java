package de.terministic.fabsim.tests.setuptests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.FabModel;

public class NegativeSetupTimeTest {

	// test, if you can set a negative SetupTime

	ToolGroup toolGroup;
	FabModel model;

	@Test
	public void NegativeTimeTest() {
		model = new FabModel();
		toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("Machine1", 1, ProcessingType.LOT);
		SetupState s1 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s1", toolGroup);
		SetupState s2 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s2", toolGroup);

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s1, s2, -5L, true, toolGroup);
		});

	}

}
