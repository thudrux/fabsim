/*
 * 
 * @author    Falk Pappert
 */
package de.terministic.fabsim.tests.operator;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.Source;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.core.AbstractOperatorGroup;
import de.terministic.fabsim.core.AbstractSink;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.IDuration;
import de.terministic.fabsim.statistics.FirstCycleTimeTracker;
import de.terministic.fabsim.statistics.ToolStateChangeLog;
import de.terministic.fabsim.statistics.ToolStateLogEntry;

public class OperatorBasicTest {

	FabModel model;
	SimulationEngine engine;
	Source source;
	Sink sink;
	AbstractHomogeneousResourceGroup toolGroup;
	AbstractOperatorGroup operatorGroup;

	@BeforeEach
	public void setUp() throws Exception {
		this.model = new FabModel();
		this.engine = new SimulationEngine();

	}

	@Test
	public void simpleOperatorBlockUsageTest() {
		sink = (Sink) this.model.getSimComponentFactory().createSink();
		this.operatorGroup = this.model.getSimComponentFactory().createOperatorGroup("OpGroup", 1);

		this.toolGroup = (AbstractHomogeneousResourceGroup) this.model.getSimComponentFactory()
				.createToolGroup("ToolGroup1", 1, ProcessingType.LOT);
		this.toolGroup.setOpProcessingPercentage(100);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, this.operatorGroup,
				0L, 7L, 0L, null, null, ProcessType.LOT, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		this.source = (Source) this.model.getSimComponentFactory().createSource("Source1", product, 45L);

		final Sink blockSink = (Sink) this.model.getSimComponentFactory().createSink("BlockSink");
		final AbstractHomogeneousResourceGroup toolGroupOpBlock = (AbstractHomogeneousResourceGroup) this.model
				.getSimComponentFactory().createToolGroup("OpBlock", 1, ProcessingType.LOT);
		toolGroupOpBlock.setOpProcessingPercentage(100);

		final Recipe blockRecipe = this.model.getSimComponentFactory().createRecipe("OpBlockRecipe");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("BlockStep", toolGroupOpBlock,
				this.operatorGroup, 0L, 50L, 0L, null, null, ProcessType.LOT, blockRecipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("BlockSinkStep", blockSink, 0L,
				ProcessType.LOT, blockRecipe);

		Product blockProduct = model.getSimComponentFactory().createProduct("Product", blockRecipe);

		final LotSource blockSource = (LotSource) this.model.getSimComponentFactory().createSource("BlockSource",
				blockProduct, 1000L);
		blockSource.setCreateFirstAtTimeZero(true);

		this.engine.init(this.model);
		final ToolStateChangeLog log = new ToolStateChangeLog();
		this.toolGroup.addListener(log);

		this.engine.runSimulation(80L);
		final List<ToolStateLogEntry> toolLogList = log.getLog().get(this.toolGroup.getToolByIndex(0));

		Assertions.assertEquals(4, toolLogList.size());

		Assertions.assertEquals(45L, toolLogList.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_OPERATOR, toolLogList.get(1).getNewState());

		Assertions.assertEquals(50L, toolLogList.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, toolLogList.get(2).getNewState());

		Assertions.assertEquals(57L, toolLogList.get(3).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, toolLogList.get(3).getNewState());
	}

	@Test
	public void operatorBlockingLoadUnloadTest() {

		AbstractSink sink1 = model.getSimComponentFactory().createSink("sink1");
		AbstractSink sink2 = model.getSimComponentFactory().createSink("sink2");

		// TG1 mit 1 Tool
		// Load Processing Unload 75%
		AbstractHomogeneousResourceGroup toolGroup1 = (AbstractHomogeneousResourceGroup) model.getSimComponentFactory()
				.createToolGroup("ToolGroup1", 1, ProcessingType.LOT);
		toolGroup1.setOpLoadPercentage(75);
		toolGroup1.setOpProcessingPercentage(75);
		toolGroup1.setOpUnloadPercentage(75);

		// TG2 mit 1 Tool
		// Load Processing Unload 75%
		AbstractHomogeneousResourceGroup toolGroup2 = (AbstractHomogeneousResourceGroup) model.getSimComponentFactory()
				.createToolGroup("ToolGroup2", 1, ProcessingType.LOT);
		toolGroup2.setOpLoadPercentage(75);
		toolGroup2.setOpProcessingPercentage(75);
		toolGroup2.setOpUnloadPercentage(75);

		// DummyTG mit 1 Tool
		AbstractToolGroup dummyToolGroup = model.getSimComponentFactory().createToolGroup("DummyGroup", 1,
				ProcessingType.LOT);

		// OG mit 1 Operator
		AbstractOperatorGroup ops = model.getSimComponentFactory().createOperatorGroup("Ops", 1);

		// Recipe 1
		// Schritt (40,60,80) auf TG1 mit Operator
		Recipe recipe1 = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step_1_1", toolGroup1, ops, 40L, 60L, 80L, null,
				null, ProcessType.LOT, recipe1);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("sinkStep_1", sink1, 0L, ProcessType.LOT,
				recipe1);

		// Source 1 Los bei 0, Intervall 500
		Product product1 = model.getSimComponentFactory().createProduct("Product1", recipe1);
		LotSource source1 = (LotSource) model.getSimComponentFactory().createSource("Source1", product1, 500L);
		source1.setCreateFirstAtTimeZero(true);

		// Recipe 2
		// Schritt (1) auf Dummy
		// Schritt (16, 24, 4) auf TG2 mit Operator
		Recipe recipe2 = model.getSimComponentFactory().createRecipe("Recipe2");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step_2_1", dummyToolGroup, 1L, null, null,
				ProcessType.LOT, recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step_2_2", toolGroup2, ops, 16L, 24L, 4L, null,
				null, ProcessType.LOT, recipe2);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("sinkStep_1", sink2, 0L, ProcessType.LOT,
				recipe2);
		// Source 1 Los bei 0, Intervall 500
		Product product2 = model.getSimComponentFactory().createProduct("Product", recipe2);
		LotSource source2 = (LotSource) model.getSimComponentFactory().createSource("Source2", product2, 500L);
		source2.setCreateFirstAtTimeZero(true);

		this.engine.init(this.model);
		ToolStateChangeLog log = new ToolStateChangeLog();
		this.engine.addListener(log);
		this.engine.runSimulation(200L);

		// Erwartung
		// Equipment Log Dummy 0=>PR 1=> SB_NO_MATERIAL
		List<ToolStateLogEntry> dummyLog = log.getLog().get(dummyToolGroup.getToolByIndex(0));

		Assertions.assertEquals(3, dummyLog.size());

		Assertions.assertEquals(0L, dummyLog.get(0).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, dummyLog.get(0).getNewState());

		Assertions.assertEquals(0L, dummyLog.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, dummyLog.get(1).getNewState());

		Assertions.assertEquals(1L, dummyLog.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, dummyLog.get(2).getNewState());

		// Equipment Log TG1 0=>PR 40=>SB_NO_OPERATOR 42=>PR 102=>SB_NO_OPERATOR 105=>PR
		// 185 => SB_NO_MATERIAL
		List<ToolStateLogEntry> tool1Log = log.getLog().get(toolGroup1.getToolByIndex(0));
		Assertions.assertEquals(7, tool1Log.size());

		Assertions.assertEquals(0L, tool1Log.get(0).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, tool1Log.get(0).getNewState());

		Assertions.assertEquals(0L, tool1Log.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, tool1Log.get(1).getNewState());

		Assertions.assertEquals(40L, tool1Log.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_OPERATOR, tool1Log.get(2).getNewState());

		Assertions.assertEquals(42L, tool1Log.get(3).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, tool1Log.get(3).getNewState());

		Assertions.assertEquals(102L, tool1Log.get(4).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_OPERATOR, tool1Log.get(4).getNewState());

		Assertions.assertEquals(105L, tool1Log.get(5).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, tool1Log.get(5).getNewState());

		Assertions.assertEquals(185L, tool1Log.get(6).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, tool1Log.get(6).getNewState());

		// Equipment Log TG2 1=>SB_NO_OPERATOR 30=>PR 46=>SB_NO_OPERATOR 87=>PR
		// 111=>SB_NO_OPERATOR 165=>PR 169=>SB_NO_MATERIAL
		List<ToolStateLogEntry> tool2Log = log.getLog().get(toolGroup2.getToolByIndex(0));
		Assertions.assertEquals(8, tool2Log.size());

		Assertions.assertEquals(0L, tool2Log.get(0).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, tool2Log.get(0).getNewState());

		Assertions.assertEquals(1L, tool2Log.get(1).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_OPERATOR, tool2Log.get(1).getNewState());

		Assertions.assertEquals(30L, tool2Log.get(2).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, tool2Log.get(2).getNewState());

		Assertions.assertEquals(46L, tool2Log.get(3).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_OPERATOR, tool2Log.get(3).getNewState());

		Assertions.assertEquals(87L, tool2Log.get(4).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, tool2Log.get(4).getNewState());

		Assertions.assertEquals(111L, tool2Log.get(5).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_OPERATOR, tool2Log.get(5).getNewState());

		Assertions.assertEquals(165L, tool2Log.get(6).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.PR, tool2Log.get(6).getNewState());

		Assertions.assertEquals(169L, tool2Log.get(7).getTime());
		Assertions.assertEquals(SemiE10EquipmentState.SB_NO_MATERIAL, tool2Log.get(7).getNewState());

	}

	@Test
	public void simpleOperatorTest() {
		sink = (Sink) this.model.getSimComponentFactory().createSink();
		this.operatorGroup = this.model.getSimComponentFactory().createOperatorGroup("OpGroup", 1);

		this.toolGroup = (AbstractHomogeneousResourceGroup) this.model.getSimComponentFactory()
				.createToolGroup("ToolGroup1", 1, ProcessingType.LOT);
		this.toolGroup.setOpProcessingPercentage(100);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, this.operatorGroup,
				0L, 7L, 0L, null, null, ProcessType.LOT, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		this.source = (Source) this.model.getSimComponentFactory().createSource("Source1", product, 45L);

		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		this.engine.init(this.model);
		this.engine.addListener(tracker);
		this.engine.runSimulation(80L);

		Assertions.assertEquals(7L, tracker.getCycleTime());

	}

	@Test
	public void operatorBreakdownTest() {
		sink = (Sink) this.model.getSimComponentFactory().createSink();
		this.operatorGroup = this.model.getSimComponentFactory().createOperatorGroup("OperatorGroup", 1);

		this.toolGroup = (AbstractHomogeneousResourceGroup) model.getSimComponentFactory().createToolGroup("ToolGroup",
				1, ProcessingType.LOT);
		this.toolGroup.setOpProcessingPercentage(100);
		IDuration mttr = model.getDurationObjectFactory().createConstantDurationObject(5L);
		IDuration mtbf = model.getDurationObjectFactory().createConstantDurationObject(47L);

		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("SmallBreakdown", mttr, mtbf,
				toolGroup);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, this.operatorGroup,
				0L, 7L, 0L, null, null, ProcessType.LOT, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		this.source = (Source) this.model.getSimComponentFactory().createSource("Source1", product, 45L);

		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		this.engine.init(this.model);
		this.engine.addListener(tracker);
		this.engine.runSimulation(80L);

		Assertions.assertEquals(12L, tracker.getCycleTime());
	}

	@Test
	public void operatorBreakdownAfterOpFinishedTest() {
		sink = (Sink) this.model.getSimComponentFactory().createSink();
		this.operatorGroup = this.model.getSimComponentFactory().createOperatorGroup("OperatorGroup", 1);

		this.toolGroup = (AbstractHomogeneousResourceGroup) model.getSimComponentFactory().createToolGroup("ToolGroup",
				1, ProcessingType.LOT);
		this.toolGroup.setOpProcessingPercentage(10);
		IDuration mttr = model.getDurationObjectFactory().createConstantDurationObject(5L);
		IDuration mtbf = model.getDurationObjectFactory().createConstantDurationObject(47L);

		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("SmallBreakdown", mttr, mtbf,
				toolGroup);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, this.operatorGroup,
				0L, 7L, 0L, null, null, ProcessType.LOT, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("lastStep", sink, 0L, ProcessType.LOT,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		this.source = (Source) this.model.getSimComponentFactory().createSource("Source1", product, 45L);

		FirstCycleTimeTracker tracker = new FirstCycleTimeTracker();
		this.engine.init(this.model);
		this.engine.addListener(tracker);
		this.engine.runSimulation(80L);

		Assertions.assertEquals(12L, tracker.getCycleTime());
	}

	@AfterEach
	public void tearDown() throws Exception {
		this.model = null;
		this.engine = null;
		this.source = null;
	}
}
