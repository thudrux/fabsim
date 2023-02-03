package de.terministic.fabsim.tests.statistics;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.IDuration;
import de.terministic.fabsim.core.eventlist.PriorityQueueEventListManager;
import de.terministic.fabsim.statistics.FlowFactorLogger;
import de.terministic.fabsim.statistics.SimulationResultAggregator;

public class FFLoggerLargeToolGroupRuntimeTest {

	private final long SECOND = 1000L;
	private final long MINUTE = 60 * SECOND;
	private final long HOUR = 60 * MINUTE;
	private final long DAY = 24 * HOUR;
	private final long YEAR = 365 * DAY;

	Random rand;

	@BeforeEach
	public void setUp() {
		rand = new Random(9472934792347249L);
	}

	private FabModel buildModel(int toolCount, long interarrivalTime) {
		FabModel model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();

		IDuration mttr = model.getDurationObjectFactory().createExponentialDurationObject(MINUTE * 200,
				rand);
		IDuration mttf = model.getDurationObjectFactory().createExponentialDurationObject(MINUTE * 800,
				rand);
		AbstractHomogeneousResourceGroup toolGroup = (ToolGroup) model.getSimComponentFactory()
				.createToolGroup("Toolgroup", toolCount, ProcessingType.BATCH);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Breakdown", mttr, mttf,
				toolGroup);

		BatchDetails batchDetails = model.getSimComponentFactory()
				.createBatchDetailsAndAddToToolGroup("BatchDetails_75_100", 75, 100, 10 * HOUR, (ToolGroup) toolGroup);
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, HOUR, batchDetails,
				ProcessType.BATCH, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		IDuration interarrival = model.getDurationObjectFactory()
				.createExponentialDurationObject(interarrivalTime * SECOND, rand);

		model.getSimComponentFactory().createSource("Source1", product, interarrival);

		return model;
	}

	@Test
	@Tag("slow")
	public void toolgroup20ToolsArrivalEveryMinuteTest() {
		long startTime = System.currentTimeMillis();
		FabModel model = buildModel(20, 60);
		PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		engine.init(model);
		SimulationResultAggregator agg = new SimulationResultAggregator();
		FlowFactorLogger ffLogger = new FlowFactorLogger();
		engine.addListener(ffLogger);
		engine.addListeners(agg.neededListeners());
		engine.runSimulation(DAY * 100);
		Assertions.assertEquals(ffLogger.getFF(), agg.generateResults(0, DAY * 100).getFlowFactor(), 0.001);
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 64000);
	}

	@Test
	@Tag("slow")
	public void toolgroup20ToolsArrivalEvery30SecondsSpeedTest() {
		long startTime = System.currentTimeMillis();
		FabModel model = buildModel(20, 30);
		PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		engine.init(model);
		FlowFactorLogger ffLogger = new FlowFactorLogger();
		engine.addListener(ffLogger);
		engine.runSimulation(DAY * 200);
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 64000);
	}

	@Test
	@Tag("slow")
	public void toolgroup20ToolsArrivalEvery30SecondsTest() {
		long startTime = System.currentTimeMillis();
		FabModel model = buildModel(20, 30);
		PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		engine.init(model);
		SimulationResultAggregator agg = new SimulationResultAggregator();
		FlowFactorLogger ffLogger = new FlowFactorLogger();
		engine.addListener(ffLogger);
		engine.addListeners(agg.neededListeners());
		engine.runSimulation(DAY * 200);
		Assertions.assertEquals(ffLogger.getFF(), agg.generateResults(0, DAY * 200).getFlowFactor(), 0.001);
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 64000);
	}

	@Test
	@Tag("slow")
	public void toolgroup20ToolsArrivalEveryMinuteSpeedTest() {
		long startTime = System.currentTimeMillis();
		FabModel model = buildModel(20, 60);
		PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
		SimulationEngine engine = new SimulationEngine(eventList);
		engine.init(model);
		FlowFactorLogger ffLogger = new FlowFactorLogger();
		engine.addListener(ffLogger);
		engine.runSimulation(DAY * 100);
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 64000);
	}

}
