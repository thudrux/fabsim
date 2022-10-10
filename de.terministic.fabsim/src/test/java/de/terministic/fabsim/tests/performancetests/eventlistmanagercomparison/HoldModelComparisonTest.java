package de.terministic.fabsim.tests.performancetests.eventlistmanagercomparison;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.eventlist.ComponentGroupedEventListManager;
import de.terministic.fabsim.core.eventlist.PriorityQueueEventListManager;
import de.terministic.fabsim.core.eventlist.TreeSetEventListManager;
import de.terministic.fabsim.core.eventlist.comparison.HoldModelBlock;

public class HoldModelComparisonTest {
	private final long SECOND = 1000L;
	private final long MINUTE = 60 * SECOND;
	private final long HOUR = 60 * MINUTE;
	private final long DAY = 24 * HOUR;
	private final long YEAR = 365 * DAY;

	Random rand = new Random();

	private FabModel buildModel(int modelSize, int componentComplexity) {
		FabModel model = new FabModel();
		for (int i = 0; i < modelSize; i++) {
			HoldModelBlock block = new HoldModelBlock(model, "");
			block.setEventCount(componentComplexity);
			block.setDuration(model.getDurationObjectFactory().createExponentialDurationObject(DAY, rand));
			model.addSpecialBlock(block);
//			model.getS
		}
		return model;
	}

	@ParameterizedTest
	@MethodSource("modelSizeAndComponentComplexityProvider")
	public void simpleHoldModelTestTest(int modelSize, int componentComplexity) {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			FabModel model = buildModel(modelSize, componentComplexity);
			ComponentGroupedEventListManager eventList = new ComponentGroupedEventListManager();
			SimulationEngine engine = new SimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;

		Assertions.assertTrue(duration < 5000);
	}

	@ParameterizedTest
	@MethodSource("modelSizeAndComponentComplexityProvider")
	public void runTimeForMiniModelWithPriorityQueueEventListManagerTest(int modelSize, int componentComplexity) {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel(modelSize, componentComplexity);
			PriorityQueueEventListManager eventList = new PriorityQueueEventListManager();
			SimulationEngine engine = new SimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(duration < 24000);
	}

	@ParameterizedTest
	@MethodSource("modelSizeAndComponentComplexityProvider")
	public void runTimeForMiniModelWithTreeSetEventListManagerTest(int modelSize, int componentComplexity) {
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			FabModel model = buildModel(modelSize, componentComplexity);
			TreeSetEventListManager eventList = new TreeSetEventListManager();
			SimulationEngine engine = new SimulationEngine(eventList);
			engine.init(model);
			engine.runSimulation(YEAR);
		}
		long duration = System.currentTimeMillis() - startTime;
		Assertions.assertTrue(duration < 24000);
	}

	static Stream<Arguments> modelSizeAndComponentComplexityProvider() {
		int[] modelSize = { 10, 50, 100, 1000 };
		int[] componentComplexity = { 5, 10, 20 };
		ArrayList<Arguments> args = new ArrayList<Arguments>();
		for (int j = 0; j < modelSize.length; j++) {
			for (int i = 0; i < componentComplexity.length; i++) {
				args.add(Arguments.of(modelSize[j], componentComplexity[i]));
			}

		}
		return args.stream();
	}
}
