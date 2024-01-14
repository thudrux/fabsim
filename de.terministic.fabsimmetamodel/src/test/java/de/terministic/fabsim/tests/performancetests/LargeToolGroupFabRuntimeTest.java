package de.terministic.fabsim.tests.performancetests;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.IDuration;
import de.terministic.fabsim.core.eventlist.PriorityQueueEventListManager;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;

public class LargeToolGroupFabRuntimeTest {

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

		IDuration mttr = model.getDurationObjectFactory().createExponentialDurationObject(MINUTE * 200, rand);
		IDuration mttf = model.getDurationObjectFactory().createExponentialDurationObject(MINUTE * 800, rand);
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
				.createExponentialDurationObject(interarrivalTime * MINUTE, rand);

		model.getSimComponentFactory().createSource("Source1", product, interarrival);

		return model;
	}

//	@Test
//	@Tag("slow")
	public void toolgroup20ToolsArrivalEvery9MinutesTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel(20, 9);
			PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 64000);
	}

//	@Test
//	@Tag("slow")
	public void toolgroup60ToolsArrivalEvery3MinutesTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel(60, 3);
			PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 64000);
	}

//	@Test
//	@Tag("slow")
	public void toolgroup180ToolsArrivalEvery1MinutesTest() {
		long startTime = System.currentTimeMillis();
		FabModel model = buildModel(180, 1);
		PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
		SimulationEngine engine = new FabSimulationEngine(eventList);
		engine.init(model);
		engine.runSimulation(10 * YEAR);
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 64000);
	}
}
