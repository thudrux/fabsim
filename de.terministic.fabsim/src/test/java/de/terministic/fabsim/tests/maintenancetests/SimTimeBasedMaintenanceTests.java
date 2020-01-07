package de.terministic.fabsim.tests.maintenancetests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.batchrules.BasicBatchRule;
import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.Controller;
import de.terministic.fabsim.components.ToolGroupController;
import de.terministic.fabsim.components.equipment.Tool;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.maintenance.SimTimeBasedMaintenance;
import de.terministic.fabsim.components.equipment.setup.AllAllowedSetupStrategy;
import de.terministic.fabsim.components.equipment.toolstatemachine.BasicToolStateMachine;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.ConstantDurationObject;
import de.terministic.fabsim.dispatchRules.FIFO;
import de.terministic.fabsim.statistics.ToolStateChangeLog;
import de.terministic.fabsim.statistics.ToolStateLogEntry;

public class SimTimeBasedMaintenanceTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBasicObjectCreation(){
		FabModel model= new FabModel();
		ConstantDurationObject obj3 = new ConstantDurationObject(3L);
		ConstantDurationObject obj5 = new ConstantDurationObject(5L);
		SimTimeBasedMaintenance maint = new SimTimeBasedMaintenance(model,"maint", obj3, obj5);
		assertEquals("maint",maint.getName());
		assertEquals(3L, maint.getDuration());
		assertEquals(5L, maint.getTimeBetweenMaintenances().getDuration());
	}

	@Test
	public void testNextOccuranceOnTool(){
		FabModel fabModel = new FabModel();
		Controller cr = new Controller(fabModel, new FIFO(), new BasicBatchRule(fabModel));
		ToolGroupController tgController = new ToolGroupController(fabModel, cr);
		ConstantDurationObject obj3 = new ConstantDurationObject(3L);
		ConstantDurationObject obj5 = new ConstantDurationObject(5L);
		SimTimeBasedMaintenanceTestUmbrella maint = new SimTimeBasedMaintenanceTestUmbrella(fabModel,"maint", obj3, obj5);
		ToolGroup toolGroup = new ToolGroup(fabModel,"Tool1", ProcessingType.LOT, 1, new BasicToolStateMachine(fabModel),tgController, new AllAllowedSetupStrategy());
		Tool tool = (Tool)toolGroup.getToolByIndex(0);
		maint.addTool(tool);
		assertEquals(5L,maint.getTimeTillNextOccuranceOnTool(tool));
	}
	
	@Test
	public void testMaintenanceTimesOnSingleTool(){
		FabModel model = new FabModel();
		ToolGroup toolGroup=(ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup", 1,ProcessingType.LOT);
		Tool tool = (Tool)toolGroup.getToolByIndex(0);
		ConstantDurationObject obj3 = new ConstantDurationObject(3L);
		ConstantDurationObject obj7 = new ConstantDurationObject(7L);
		SimTimeBasedMaintenance maint =model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup("Maintenance1",obj3,obj7, toolGroup);
		maint.setFirstOccuranceForTool(tool, 5L);
		EventListManager eventList= new EventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);

		ToolStateChangeLog log = new ToolStateChangeLog();  
		engine.init(model);
		tool.addListener(log);
		engine.runSimulation(100L);
		List<ToolStateLogEntry> toolLog =log.getLog().get(tool);
		assertEquals(21, toolLog.size());
		assertEquals(5L, toolLog.get(1).getTime());

	}
}
