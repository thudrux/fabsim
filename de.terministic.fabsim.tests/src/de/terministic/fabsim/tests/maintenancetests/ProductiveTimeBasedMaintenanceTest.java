package de.terministic.fabsim.tests.maintenancetests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.Tool;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.breakdown.SimTimeBasedBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.ProcessTimeBasedMaintenance;
import de.terministic.fabsim.core.AbstractSink;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.ConstantDurationObject;
import de.terministic.fabsim.statistics.ToolStateChangeLog;
import de.terministic.fabsim.statistics.ToolStateLogEntry;

public class ProductiveTimeBasedMaintenanceTest {

	@Test
	public void testProductiveTimeBasedMaintenanceTimesOnSingleTool(){
		FabModel model = new FabModel();
		ToolGroup toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup", 1,ProcessingType.LOT);
		Tool tool = (Tool)toolGroup.getToolByIndex(0);
		AbstractSink sink = model.getSimComponentFactory().createSink();

		ConstantDurationObject obj3 = new ConstantDurationObject(20L);
		ConstantDurationObject obj7 = new ConstantDurationObject(17L);
		ProcessTimeBasedMaintenance maint =model.getSimComponentFactory().createProcessTimeBasedMaintenanceAndAddToToolGroup("Maintenance1",obj3,obj7, toolGroup);

		EventListManager eventList= new EventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 7L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink step", sink, 0L, ProcessType.LOT, recipe);
		
		ConstantDurationObject obj= model.getDurationObjectFactory().createConstantDurationObject(13L);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);
		LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source_1", product, obj);
		source.setCreateFirstAtTimeZero(true);

		ToolStateChangeLog log = new ToolStateChangeLog();  
		engine.init(model);
		tool.addListener(log);
		engine.runSimulation(60L);
		List<ToolStateLogEntry> toolLog =log.getLog().get(tool);
		for(ToolStateLogEntry entry:toolLog){
			System.out.println(entry);
		}
		assertEquals(12, toolLog.size());
		assertEquals(0L, toolLog.get(0).getTime());
		assertEquals(0L, toolLog.get(1).getTime());
		assertEquals(7L, toolLog.get(2).getTime());
		assertEquals(13L, toolLog.get(3).getTime());
		assertEquals(20L, toolLog.get(4).getTime());
		assertEquals(26L, toolLog.get(5).getTime());
		assertEquals(33L, toolLog.get(6).getTime());
		assertEquals(33L, toolLog.get(7).getTime());
		assertEquals(53L, toolLog.get(8).getTime());
		assertEquals(53L, toolLog.get(9).getTime());
		assertEquals(60L, toolLog.get(10).getTime());
		assertEquals(60L, toolLog.get(11).getTime());
	}
	
	@Test
	public void stackedFinishTest(){
		FabModel model = new FabModel();
		ToolGroup toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup", 1,ProcessingType.LOT);
		Tool tool = (Tool)toolGroup.getToolByIndex(0);
		AbstractSink sink = model.getSimComponentFactory().createSink();

		ConstantDurationObject obj3 = new ConstantDurationObject(9L);
		ConstantDurationObject obj7 = new ConstantDurationObject(7L);
		ProcessTimeBasedMaintenance maint =model.getSimComponentFactory().createProcessTimeBasedMaintenanceAndAddToToolGroup("Maintenance1",obj3,obj7, toolGroup);

		ConstantDurationObject obj4 = new ConstantDurationObject(10L);
		ConstantDurationObject obj8 = new ConstantDurationObject(10L);
		SimTimeBasedBreakdown breakdown = model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Breakdown", obj4, obj8, toolGroup);
		
		EventListManager eventList= new EventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 10L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink step", sink, 0L, ProcessType.LOT, recipe);
		
		ConstantDurationObject obj= model.getDurationObjectFactory().createConstantDurationObject(130L);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);
		LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source_1", product, obj);
		source.setCreateFirstAtTimeZero(true);

		ToolStateChangeLog log = new ToolStateChangeLog();  
		engine.init(model);
		tool.addListener(log);
		engine.runSimulation(30L);
		List<ToolStateLogEntry> toolLog =log.getLog().get(tool);
		for(ToolStateLogEntry entry:toolLog){
			System.out.println(entry);
		}
		assertEquals(0L, toolLog.get(0).getTime());
		assertEquals(0L, toolLog.get(1).getTime());
		assertEquals(10L, toolLog.get(2).getTime());
		assertEquals(20L, toolLog.get(3).getTime());
		assertEquals(20L, toolLog.get(4).getTime());
		assertEquals(20L, toolLog.get(5).getTime());
		assertEquals(29L, toolLog.get(6).getTime());
		assertEquals(30L, toolLog.get(7).getTime());
	}

	@Test
	public void allStackedFinishTest(){
		FabModel model = new FabModel();
		ToolGroup toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup", 1,ProcessingType.LOT);
		Tool tool = (Tool)toolGroup.getToolByIndex(0);
		AbstractSink sink = model.getSimComponentFactory().createSink();

		ConstantDurationObject obj3 = new ConstantDurationObject(7L);
		ConstantDurationObject obj7 = new ConstantDurationObject(10L);
		ProcessTimeBasedMaintenance maint =model.getSimComponentFactory().createProcessTimeBasedMaintenanceAndAddToToolGroup("Maintenance1",obj3,obj7, toolGroup);

		ConstantDurationObject obj4 = new ConstantDurationObject(10L);
		ConstantDurationObject obj8 = new ConstantDurationObject(10L);
		SimTimeBasedBreakdown breakdown = model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Breakdown", obj4, obj8, toolGroup);
		
		EventListManager eventList= new EventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 10L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink step", sink, 0L, ProcessType.LOT, recipe);
		
		ConstantDurationObject obj= model.getDurationObjectFactory().createConstantDurationObject(130L);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);
		LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source_1", product, obj);
		source.setCreateFirstAtTimeZero(true);

		ToolStateChangeLog log = new ToolStateChangeLog();  
		engine.init(model);
		tool.addListener(log);
		engine.runSimulation(30L);
		List<ToolStateLogEntry> toolLog =log.getLog().get(tool);
		for(ToolStateLogEntry entry:toolLog){
			System.out.println(entry);
		}
		assertEquals(0L, toolLog.get(0).getTime());
		assertEquals(0L, toolLog.get(1).getTime());
		assertEquals(10L, toolLog.get(2).getTime());
		assertEquals(20L, toolLog.get(3).getTime());
		assertEquals(20L, toolLog.get(4).getTime());
		assertEquals(20L, toolLog.get(5).getTime());
		assertEquals(27L, toolLog.get(6).getTime());
		assertEquals(30L, toolLog.get(7).getTime());
	}

	
	@Test
	public void breakdownWhileMaintainingTest(){
		FabModel model = new FabModel();
		ToolGroup toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup", 1,ProcessingType.LOT);
		Tool tool = (Tool)toolGroup.getToolByIndex(0);
		AbstractSink sink = model.getSimComponentFactory().createSink();

		ConstantDurationObject obj3 = new ConstantDurationObject(7L);
		ConstantDurationObject obj7 = new ConstantDurationObject(7L);
		ProcessTimeBasedMaintenance maint =model.getSimComponentFactory().createProcessTimeBasedMaintenanceAndAddToToolGroup("Maintenance1",obj3,obj7, toolGroup);

		ConstantDurationObject obj4 = new ConstantDurationObject(5L);
		ConstantDurationObject obj8 = new ConstantDurationObject(25L);
		SimTimeBasedBreakdown breakdown = model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Breakdown", obj4, obj8, toolGroup);
		breakdown.setFirstDefaultOccurance(13L);
		
		EventListManager eventList= new EventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 10L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink step", sink, 0L, ProcessType.LOT, recipe);
		
		ConstantDurationObject obj= model.getDurationObjectFactory().createConstantDurationObject(30L);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);
		LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source_1", product, obj);
		source.setCreateFirstAtTimeZero(true);

		ToolStateChangeLog log = new ToolStateChangeLog();  
		engine.init(model);
		tool.addListener(log);
		engine.runSimulation(30L);
		List<ToolStateLogEntry> toolLog =log.getLog().get(tool);
		for(ToolStateLogEntry entry:toolLog){
			System.out.println(entry);
		}
		assertEquals(0L, toolLog.get(0).getTime());
		assertEquals(0L, toolLog.get(1).getTime());
		assertEquals(10L, toolLog.get(2).getTime());
		assertEquals(10L, toolLog.get(3).getTime());
		assertEquals(13L, toolLog.get(4).getTime());
		assertEquals(18L, toolLog.get(5).getTime());
		assertEquals(22L, toolLog.get(6).getTime());
		assertEquals(30L, toolLog.get(7).getTime());
	}
	
	@Test
	public void breakdownWhileProcessingTest(){
		FabModel model = new FabModel();
		ToolGroup toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup", 1,ProcessingType.LOT);
		Tool tool = (Tool)toolGroup.getToolByIndex(0);
		AbstractSink sink = model.getSimComponentFactory().createSink();

		ConstantDurationObject obj3 = new ConstantDurationObject(7L);
		ConstantDurationObject obj7 = new ConstantDurationObject(12L);
		ProcessTimeBasedMaintenance maint =model.getSimComponentFactory().createProcessTimeBasedMaintenanceAndAddToToolGroup("Maintenance1",obj3,obj7, toolGroup);

		ConstantDurationObject obj4 = new ConstantDurationObject(5L);
		ConstantDurationObject obj8 = new ConstantDurationObject(25L);
		SimTimeBasedBreakdown breakdown = model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Breakdown", obj4, obj8, toolGroup);
		breakdown.setFirstDefaultOccurance(5L);
		
		EventListManager eventList= new EventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 10L, ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink step", sink, 0L, ProcessType.LOT, recipe);
		
		ConstantDurationObject obj= model.getDurationObjectFactory().createConstantDurationObject(30L);
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);
		LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source_1", product, obj);
		source.setCreateFirstAtTimeZero(true);

		ToolStateChangeLog log = new ToolStateChangeLog();  
		engine.init(model);
		tool.addListener(log);
		engine.runSimulation(70L);
		List<ToolStateLogEntry> toolLog =log.getLog().get(tool);
		for(ToolStateLogEntry entry:toolLog){
			System.out.println(entry);
		}
		assertEquals(0L, toolLog.get(0).getTime());//SB
		assertEquals(0L, toolLog.get(1).getTime());//PR
		assertEquals(5L, toolLog.get(2).getTime());//UD
		assertEquals(10L, toolLog.get(3).getTime());//PR
		assertEquals(15L, toolLog.get(4).getTime());//SB
		assertEquals(30L, toolLog.get(5).getTime());//PR
		assertEquals(35L, toolLog.get(6).getTime());//UD
		assertEquals(40L, toolLog.get(7).getTime());//PR
		assertEquals(45L, toolLog.get(8).getTime());//SB
		assertEquals(45L, toolLog.get(9).getTime());//SD_MAINT
		assertEquals(52L, toolLog.get(10).getTime());//SB
		assertEquals(60L, toolLog.get(11).getTime());//PR
		assertEquals(65L, toolLog.get(12).getTime());//UD
	}
}
