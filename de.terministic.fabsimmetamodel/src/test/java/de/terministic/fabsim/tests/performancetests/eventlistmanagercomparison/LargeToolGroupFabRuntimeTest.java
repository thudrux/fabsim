package de.terministic.fabsim.tests.performancetests.eventlistmanagercomparison;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.core.eventlist.ComponentGroupedEventListManager;
import de.terministic.fabsim.core.eventlist.PriorityQueueEventListManager;
import de.terministic.fabsim.core.eventlist.TreeSetEventListManager;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;

@Disabled("Disabled, includes only performance tests")
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

	private FabModel buildModel() {
		FabModel model = new FabModel();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();

		IValue mttr = model.getValueObjectFactory().createExponentialValueObject(MINUTE * 200,
				rand);
		IValue mttf = model.getValueObjectFactory().createExponentialValueObject(MINUTE * 800,
				rand);
		AbstractHomogeneousResourceGroup toolGroup = (ToolGroup) model.getSimComponentFactory()
				.createToolGroup("Toolgroup", 20, ProcessingType.BATCH);
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Breakdown", mttr, mttf,
				toolGroup);

		BatchDetails batchDetails = model.getSimComponentFactory()
				.createBatchDetailsAndAddToToolGroup("BatchDetails_75_100", 75, 100, 10 * HOUR, (ToolGroup) toolGroup);
		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe1");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", toolGroup, HOUR, batchDetails,
				ProcessType.BATCH, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", sink, 0L, ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		IValue interarrival = model.getValueObjectFactory()
				.createExponentialValueObject(9 * MINUTE, rand);

		model.getSimComponentFactory().createSource("Source1", product, interarrival);

		return model;
	}

	@Test
	@Tag("slow")
	public void runTimeForMiniModelWithComponentGroupedEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			ComponentGroupedEventListManager eventList = new ComponentGroupedEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
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
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(duration < 24000);
	}

	@Test
	@Tag("slow")
	public void runTimeForMiniModelWithTreeSetEventListManagerTest() {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel();
			TreeSetEventListManager eventList = new TreeSetEventListManager();
			SimulationEngine engine = new FabSimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(duration < 24000);
	}
}
