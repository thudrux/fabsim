package de.terministic.fabsim.tests.modeltests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.ProcessStep;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.Source;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.TimeGroupedEventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.ConstantDurationObject;
import de.terministic.fabsim.statistics.FirstCycleTimeTracker;
import de.terministic.fabsim.statistics.FlowItemCounter;

public class DelayTest {

	FabModel model;
	SimulationEngine engine;
	Source source;
	ToolGroup toolGroup;
	ToolGroup toolGroup2;
	ProcessStep tgStep1;
	ProcessStep tgStep2;

	@BeforeEach
	public void setUp() throws Exception {
		model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();
		toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup1");
		toolGroup2 = (ToolGroup) model.getSimComponentFactory().createToolGroup("Toolgroup2");
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		tgStep1 = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, 2L,
				ProcessType.LOT, recipe);
		tgStep2 = model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", toolGroup2, 3L,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		source = (Source) model.getSimComponentFactory().createSource("Source1", product, 1L);

		TimeGroupedEventListManager eventList = new TimeGroupedEventListManager();
		engine = new SimulationEngine(eventList);
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void countTestWithTwoMachines() {
		FlowItemCounter counter = new FlowItemCounter();
		engine.init(model);
		engine.addListener(counter);
		engine.runSimulation(25L); // 25 runs, so 25 lots
		Assertions.assertEquals(25L, counter.getItemCount()); // in 25 runs are 25 lots generated
	}

	@Test
	public void processStepTimeTestWithTwoMachines() {
		// FlowItemCounter counter = new FlowItemCounter();
		// engine.init(model);
		// engine.addListener(counter);
		// engine.runSimulation(10L);
		Assertions.assertEquals(2L, tgStep1.getDuration(null)); // test if the delay of machine 1 is 2 seconds
	}

	@Test
	public void cycleTimeTestOneJobWithTwoMachines() {
		source.setCreateFirstAtTimeZero(true);
		source.setInterArrivalTime(model.getDurationObjectFactory().createConstantDurationObject(10L));
		FirstCycleTimeTracker cycleTime = new FirstCycleTimeTracker();
		engine.init(model);
		engine.addListener(cycleTime);
		engine.runSimulation(7L);
		Assertions.assertEquals(5L, cycleTime.getCycleTime());

	}

	@Test
	public void cycleTimeChangeDurationWithTwoMachines() {
		FirstCycleTimeTracker cycleTime = new FirstCycleTimeTracker();
		// set the delays of the machines
		long durationStep1 = 7L;
		long durationStep2 = 11L;
		tgStep1.setDuration(model.getDurationObjectFactory().createConstantDurationObject(durationStep1));
		tgStep2.setDuration(model.getDurationObjectFactory().createConstantDurationObject(durationStep2));
		engine.init(model);
		engine.addListener(cycleTime);
		engine.runSimulation(200L); // run time of simulation should not matter
		Assertions.assertEquals((durationStep1 + durationStep2), cycleTime.getCycleTime());
//		System.out.println(durationStep1+durationStep2);
	}

	@Test
	public void countSinkTestWithTwoMachines() {
		FlowItemCounter counter = new FlowItemCounter();
//		FlowItemDestructionEvent destruct = new FlowItemDestructionEvent();
		FirstCycleTimeTracker cycleTime = new FirstCycleTimeTracker();
		engine.init(model);
		engine.addListener(counter);
		engine.runSimulation(5L);
		// System.out.println( cycleTime.getCycleTime());
		Assertions.assertEquals(5L, counter.getItemCount()); // in 5 sec are 5 lots generated
		// assertEquals(1L, cycleTime.processEvent()); // in 5 sec is only 1 lot to sink
	}

}
