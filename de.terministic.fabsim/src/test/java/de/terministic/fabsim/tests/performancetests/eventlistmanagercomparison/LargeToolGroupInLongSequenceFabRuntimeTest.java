package de.terministic.fabsim.tests.performancetests.eventlistmanagercomparison;

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
import de.terministic.fabsim.core.ComponentGroupedEventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.PriorityQueueEventListManager;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.TreeSetEventListManager;
import de.terministic.fabsim.core.duration.AbstractDurationObject;

public class LargeToolGroupInLongSequenceFabRuntimeTest {

	private final long SECOND = 1000L;
	private final long MINUTE = 60 * SECOND;
	private final long HOUR = 60 * MINUTE;
	private final long DAY = 24 * HOUR;
	private final long YEAR = 365 * DAY;

	private void createToolgroupAndAddAStepToRecipe(int i, Recipe recipe, FabModel model) {
		AbstractDurationObject mttr = model.getDurationObjectFactory().createExponentialDurationObject(MINUTE * 200,
				rand);
		AbstractDurationObject mttf = model.getDurationObjectFactory().createExponentialDurationObject(MINUTE * 800,
				rand);
		AbstractHomogeneousResourceGroup toolGroup = (ToolGroup) model.getSimComponentFactory()
				.createToolGroup("Toolgroup_" + i, 10, ProcessingType.BATCH);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Breakdown", mttr, mttf,
				toolGroup);
		BatchDetails batchDetails = model.getSimComponentFactory()
				.createBatchDetailsAndAddToToolGroup("BatchDetails_75_100", 75, 100, 10 * HOUR, (ToolGroup) toolGroup);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step" + i, toolGroup, HOUR, batchDetails,
				ProcessType.BATCH, recipe);

	}

	private FabModel buildModel() {
		FabModel model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		for (int i = 1; i <= 100; i++) {
			createToolgroupAndAddAStepToRecipe(i, recipe, model);
		}
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		AbstractDurationObject interarrival = model.getDurationObjectFactory()
				.createExponentialDurationObject(18 * MINUTE, rand);

		model.getSimComponentFactory().createSource("Source1", product, interarrival);

		return model;
	}

	Random rand;

	@BeforeEach
	public void setUp() {
		rand = new Random(9472934792347249L);
	}

//	@Test
//	@Tag("slow")
	public void runTimeForMiniModelWithComponentGroupedEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			FabModel model = buildModel();
			ComponentGroupedEventListManager eventList = new ComponentGroupedEventListManager();
			SimulationEngine engine = new SimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(duration < 24000);
	}

	@Test
	@Tag("slow")
	public void runTimeForMiniModelWithPriorityQueueEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			FabModel model = buildModel();
			PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
			SimulationEngine engine = new SimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(duration < 24000);
	}

//	@Test
//	@Tag("slow")
	public void runTimeForMiniModelWithTreeSetEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			FabModel model = buildModel();
			TreeSetEventListManager eventList = new TreeSetEventListManager();
			SimulationEngine engine = new SimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(duration < 24000);
	}
}
