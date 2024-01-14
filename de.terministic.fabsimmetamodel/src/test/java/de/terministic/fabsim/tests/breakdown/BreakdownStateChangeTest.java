package de.terministic.fabsim.tests.breakdown;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.IDuration;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.statistics.ToolStateChangeLog;
import de.terministic.fabsim.metamodel.statistics.ToolStateLogEntry;

public class BreakdownStateChangeTest {
	FabModel model;
	SimulationEngine engine;
	LotSource source;
	AbstractHomogeneousResourceGroup toolGroup;
	ProcessStep tgStep;
	SetupState s2;

	@BeforeEach
	public void setUp() throws Exception {

		model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		toolGroup = (AbstractHomogeneousResourceGroup) model.getSimComponentFactory().createToolGroup("Toolgroup");
		SetupState s1 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s1", (ToolGroup) toolGroup);
		s2 = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("s2", (ToolGroup) toolGroup);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(s2, s1, 13L, (ToolGroup) toolGroup);
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		tgStep = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 7L, s1,
				ProcessType.BATCH, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.BATCH, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (LotSource) model.getSimComponentFactory().createSource("Source1", product, 50L);

		engine = new FabSimulationEngine();
	}

	@AfterEach
	public void tearDown() throws Exception {
		model = null;
		engine = null;
		toolGroup = null;
		tgStep = null;
		source = null;
	}

	@Test
	public void breakdownStateWhileToolIsIdleTest() {
		IDuration mttr = model.getDurationObjectFactory().createConstantDurationObject(5L);
		IDuration mtbf = model.getDurationObjectFactory().createConstantDurationObject(23L);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("SmallBreakdown", mttr, mtbf,
				toolGroup);

		engine.init(model);
		ToolStateChangeLog log = new ToolStateChangeLog();
		toolGroup.addListener(log);

		engine.runSimulation(49L);
		List<ToolStateLogEntry> toolLogList = log.getLog().get(toolGroup.getToolByIndex(0));

		Assertions.assertEquals(3, toolLogList.size());

		Assertions.assertEquals(23L, toolLogList.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.UD, toolLogList.get(1).getNewState());

		Assertions.assertEquals(28L, toolLogList.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, toolLogList.get(2).getNewState());
	}

	@Test
	public void breakdownStateWhileToolIsProductiveTest() {
		IDuration mttr = model.getDurationObjectFactory().createConstantDurationObject(5L);
		IDuration mtbf = model.getDurationObjectFactory().createConstantDurationObject(53L);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("SmallBreakdown", mttr, mtbf,
				toolGroup);

		engine.init(model);
		ToolStateChangeLog log = new ToolStateChangeLog();
		toolGroup.addListener(log);

		engine.runSimulation(80L);
		List<ToolStateLogEntry> toolLogList = log.getLog().get(toolGroup.getToolByIndex(0));

		Assertions.assertEquals(5, toolLogList.size());

		Assertions.assertEquals(50L, toolLogList.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, toolLogList.get(1).getNewState());

		Assertions.assertEquals(53L, toolLogList.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.UD, toolLogList.get(2).getNewState());

		Assertions.assertEquals(58L, toolLogList.get(3).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, toolLogList.get(3).getNewState());

		Assertions.assertEquals(62L, toolLogList.get(4).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, toolLogList.get(4).getNewState());
	}

	@Test
	public void breakdownStateWhileToolIsInMaintenanceTest() {
		IDuration obj7 = model.getDurationObjectFactory().createConstantDurationObject(7L);
		IDuration obj19 = model.getDurationObjectFactory().createConstantDurationObject(19L);
		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup("SmallMaintenance", obj7,
				obj19, (ToolGroup) toolGroup);
		IDuration mttr = model.getDurationObjectFactory().createConstantDurationObject(5L);
		IDuration mtbf = model.getDurationObjectFactory().createConstantDurationObject(20L);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("SmallBreakdown", mttr, mtbf,
				toolGroup);

		engine.init(model);
		ToolStateChangeLog log = new ToolStateChangeLog();
		toolGroup.addListener(log);

		engine.runSimulation(40L);
		List<ToolStateLogEntry> toolLogList = log.getLog().get(toolGroup.getToolByIndex(0));

		Assertions.assertEquals(5, toolLogList.size());

		Assertions.assertEquals(19L, toolLogList.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SD_MAINT, toolLogList.get(1).getNewState());

		Assertions.assertEquals(20L, toolLogList.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.UD, toolLogList.get(2).getNewState());

		Assertions.assertEquals(25L, toolLogList.get(3).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SD_MAINT, toolLogList.get(3).getNewState());

		Assertions.assertEquals(31L, toolLogList.get(4).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, toolLogList.get(4).getNewState());
	}

	@Test
	public void breakdownStateWhileToolIsInSetupTest() {
		toolGroup.setInitialSetup(s2);
		IDuration mttr = model.getDurationObjectFactory().createConstantDurationObject(5L);
		IDuration mtbf = model.getDurationObjectFactory().createConstantDurationObject(53L);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("SmallBreakdown", mttr, mtbf,
				toolGroup);

		engine.init(model);
		ToolStateChangeLog log = new ToolStateChangeLog();
		toolGroup.addListener(log);

		engine.runSimulation(80L);
		List<ToolStateLogEntry> toolLogList = log.getLog().get(toolGroup.getToolByIndex(0));

		Assertions.assertEquals(6, toolLogList.size());

		Assertions.assertEquals(50L, toolLogList.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SD_SETUP, toolLogList.get(1).getNewState());

		Assertions.assertEquals(53L, toolLogList.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.UD, toolLogList.get(2).getNewState());

		Assertions.assertEquals(58L, toolLogList.get(3).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SD_SETUP, toolLogList.get(3).getNewState());

		Assertions.assertEquals(68L, toolLogList.get(4).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, toolLogList.get(4).getNewState());

		Assertions.assertEquals(75L, toolLogList.get(5).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, toolLogList.get(5).getNewState());
	}

	@Test
	public void breakdownDuringToolBreakdownWhileProductiveTest() {
		IDuration mttr1 = model.getDurationObjectFactory().createConstantDurationObject(3L);
		IDuration mtbf1 = model.getDurationObjectFactory().createConstantDurationObject(53L);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("FirstBreakdown", mttr1,
				mtbf1, toolGroup);

		IDuration mttr2 = model.getDurationObjectFactory().createConstantDurationObject(5L);
		IDuration mtbf2 = model.getDurationObjectFactory().createConstantDurationObject(55L);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("SecondBreakdown", mttr2,
				mtbf2, toolGroup);

		engine.init(model);
		ToolStateChangeLog log = new ToolStateChangeLog();
		toolGroup.addListener(log);

		engine.runSimulation(80L);
		List<ToolStateLogEntry> toolLogList = log.getLog().get(toolGroup.getToolByIndex(0));
//		for (ToolStateLogEntry entry : toolLogList) {
//			System.out.println(entry);
//		}

		Assertions.assertEquals(6, toolLogList.size());

		Assertions.assertEquals(50L, toolLogList.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, toolLogList.get(1).getNewState());

		Assertions.assertEquals(53L, toolLogList.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.UD, toolLogList.get(2).getNewState());

		Assertions.assertEquals(56L, toolLogList.get(3).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.UD, toolLogList.get(3).getNewState());

		Assertions.assertEquals(61L, toolLogList.get(4).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, toolLogList.get(4).getNewState());

		Assertions.assertEquals(65L, toolLogList.get(5).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, toolLogList.get(5).getNewState());

	}

}
