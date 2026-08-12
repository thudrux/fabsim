package de.terministic.fabsim.metamodel.examples.minifab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.ExponentialDuration;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.ExternalDispatchRule;
import de.terministic.fabsim.metamodel.examples.results.RunResult;
import de.terministic.fabsim.metamodel.examples.results.RunResultAggregator;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;
import de.terministic.fabsim.metamodel.logging.LoggingDispatchRule;
import de.terministic.fabsim.metamodel.statistics.FinishedLotStatisticsCollector;


public class MiniFab {
	static final long SECOND = 1000L;
	static final long MINUTE = 60L * SECOND;
	static final long HOUR = 60L * MINUTE;

	static final String PRODUCT_PA = "Pa";
	static final String PRODUCT_PB = "Pb";
	static final String PRODUCT_TW = "TW";

	static final String STEP_S1 = "S1";
	static final String STEP_S2 = "S2";
	static final String STEP_S3 = "S3";
	static final String STEP_S4 = "S4";
	static final String STEP_S5 = "S5";
	static final String STEP_S6 = "S6";
	static final String STEP_SINK = "Sink";

	private static final String SETUP_SEPARATOR = "_";

	private static final int LOT_SIZE = 25;

	public static final double FLOW_FACTOR = MiniFabProducts.FLOW_FACTOR;
	private static final double PROCESS_TIME_VARIATION = 0.15;


	public static final int LOTS_PER_STATION1_BATCH = 3;
	private static final int STATION1_BATCH_SIZE = LOT_SIZE * LOTS_PER_STATION1_BATCH;
	private static final long STATION1_BATCH_MAX_WAIT = 1440L * MINUTE;

	private static final long STATION1_LOAD = 20L * MINUTE;
	private static final long STATION1_UNLOAD = 40L * MINUTE;
	private static final long STATION1_MAINTENANCE_DURATION = 75L * MINUTE;
	private static final long STATION1_MAINTENANCE_INTERVAL = 1440L * MINUTE;

	private static final long STATION2_LOAD = 15L * MINUTE;
	private static final long STATION2_UNLOAD = 15L * MINUTE;
	private static final long STATION2_MAINTENANCE_DURATION = 120L * MINUTE;
	private static final long STATION2_MAINTENANCE_INTERVAL = 720L * MINUTE;

	private static final long STATION2_BREAKDOWN_MIN = 1440L * MINUTE;
	private static final long STATION2_BREAKDOWN_MAX = 4560L * MINUTE;
	private static final long STATION2_REPAIR_MIN = 360L * MINUTE;
	private static final long STATION2_REPAIR_MAX = 480L * MINUTE;

	private static final long STATION3_LOAD = 10L * MINUTE;
	private static final long STATION3_UNLOAD = 10L * MINUTE;
	private static final long STATION3_MAINTENANCE_DURATION = 30L * MINUTE;
	private static final long STATION3_MAINTENANCE_INTERVAL = 720L * MINUTE;

	private static final long STATION3_SETUP_TIME_SAME_LOT_TYPE = 10L * MINUTE;
	private static final long STATION3_SETUP_TIME_SAME_STEP = 5L * MINUTE;
	private static final long STATION3_SETUP_TIME_DEFAULT = 12L * MINUTE;

	private ToolGroup station1;
	private ToolGroup station2;
	private ToolGroup station3;
	private BatchDetails station1Step1Batch;
	private BatchDetails station1Step5Batch;
	private Map<String, SetupState> station3SetupStates;
	private FabModel model;
	private DispatchMode dispatchMode;
	private AbstractDispatchRule dispatchRule;
	private DispatchProvider dispatchProvider;
	private LocalLogWriter logWriter;

	private enum DispatchMode {
		LOCAL,
		EXTERNAL
	}

	static String recipeName(final String productName) {
		return productName + "Recipe";
	}

	static String sourceName(final String productName) {
		return "Source_" + productName;
	}

	static String station3SetupName(final String productName, final String stepName) {
		return productName + SETUP_SEPARATOR + stepName;
	}

	public MiniFab(final DispatchProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider must not be null");
		}
		this.dispatchMode = DispatchMode.EXTERNAL;
		this.dispatchProvider = provider;
		this.dispatchRule = null;
		this.logWriter = null;
		this.model = createFabModel();
	}

	public MiniFab(final AbstractDispatchRule dispatchRule,
			final LocalLogWriter logWriter) {
		if (dispatchRule == null) {
			throw new IllegalArgumentException("dispatchRule must not be null");
		}
		this.dispatchMode = DispatchMode.LOCAL;
		this.dispatchRule = dispatchRule;
		this.dispatchProvider = null;
		this.logWriter = logWriter;
		this.model = createFabModel();
	}

	private FabModel createFabModel() {
		final AbstractDispatchRule effectiveRule;
		switch (this.dispatchMode) {
			case EXTERNAL:
				effectiveRule = new ExternalDispatchRule("MiniFabExternalDispatch",
						this.dispatchProvider, FLOW_FACTOR);
				break;
			case LOCAL:
				if (this.dispatchRule instanceof LoggingDispatchRule || this.logWriter == null) {
					effectiveRule = this.dispatchRule;
				} else {
					effectiveRule = new LoggingDispatchRule(this.dispatchRule, this.logWriter, FLOW_FACTOR);
				}
				break;
			default:
				throw new IllegalStateException("MiniFab must be built before creating a model");
		}
		return assembleMiniFabModel(new FabModel(), effectiveRule);
	}

	// ---------------------------------------------------------------------
	// Simulation execution
	// ---------------------------------------------------------------------

	public RunResult run(final long simulationTimeHours, final int runs, final long warmupTimeHours) {
		if (runs <= 0) {
			throw new IllegalArgumentException("runs must be a positive number");
		}
		if (this.model == null) {
			throw new IllegalStateException("MiniFab must be built before running the simulation");
		}
		if (runs > 1 && (this.logWriter != null || this.dispatchRule instanceof LoggingDispatchRule)) {
			throw new IllegalArgumentException("Logging is only supported for a single MiniFab run");
		}
		validateTiming(simulationTimeHours, warmupTimeHours);

		final List<RunResult> runResults = new ArrayList<>(runs);
		if (runs == 1) {
			runResults.add(runSimulation(this.model, simulationTimeHours, warmupTimeHours));
		} else {
			for (int run = 0; run < runs; run++) {
				this.model = createFabModel();
				runResults.add(runSimulation(this.model, simulationTimeHours, warmupTimeHours));
			}
		}
		return RunResultAggregator.aggregate(runs, simulationTimeHours, runResults);
	}

	private RunResult runSimulation(final FabModel model, final long simulationTimeHours,
			final long warmupTimeHours) {
		final SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		final long simulationTimeMillis = Math.multiplyExact(simulationTimeHours, HOUR);
		final long warmupTimeMillis = Math.multiplyExact(warmupTimeHours, HOUR);
		final long measurementTimeHours = Math.subtractExact(simulationTimeHours, warmupTimeHours);
		final FinishedLotStatisticsCollector finishedLotStatisticsCollector = new FinishedLotStatisticsCollector(model,
				MiniFabProducts.PRIORITY_WEIGHTS, warmupTimeMillis);
		engine.addListener(finishedLotStatisticsCollector);
		engine.runSimulation(simulationTimeMillis);
		return new RunResult(simulationTimeHours, measurementTimeHours,
				finishedLotStatisticsCollector.getFinishedWafers(),
				finishedLotStatisticsCollector.getTardyWafers(),
				finishedLotStatisticsCollector.getTardinessPerWaferMinutes(),
				finishedLotStatisticsCollector.getFlowFactorMean());
	}

	private void validateTiming(final long simulationTimeHours, final long warmupTimeHours) {
		if (simulationTimeHours <= 0L) {
			throw new IllegalArgumentException("simulationTimeHours must be a positive number");
		}
		if (warmupTimeHours < 0L) {
			throw new IllegalArgumentException("warmupTimeHours must not be negative");
		}
		if (warmupTimeHours >= simulationTimeHours) {
			throw new IllegalArgumentException("warmupTimeHours must be less than simulationTimeHours");
		}
	}

	// ---------------------------------------------------------------------
	// Model assembly
	// ---------------------------------------------------------------------

	private FabModel assembleMiniFabModel(final FabModel model, final AbstractDispatchRule dispatchRule) {
		final Sink sink = (Sink) model.getSimComponentFactory().createSink(STEP_SINK);

		this.station1 = createStation1(model);
		this.station2 = createStation2(model);
		this.station3 = createStation3(model);
		this.station1.setDispatchRule(dispatchRule);
		this.station1.setBatchRule(new MiniFabBatchRule(model));
		this.station2.setDispatchRule(dispatchRule);
		this.station3.setDispatchRule(dispatchRule);
		configureStation3Setups(model);
		createRecipesAndSources(model, sink);

		return model;
	}

	// ---------------------------------------------------------------------
	// Station configuration
	// ---------------------------------------------------------------------

	private ToolGroup createStation1(final FabModel model) {
		final ToolGroup station = (ToolGroup) model.getSimComponentFactory().createToolGroup("Station1", 2,
				ProcessingType.BATCH);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
				"Station1_PreventiveMaintenance",
				model.getValueObjectFactory().createConstantValueObject(STATION1_MAINTENANCE_DURATION),
				model.getValueObjectFactory().createConstantValueObject(STATION1_MAINTENANCE_INTERVAL), station);
		this.station1Step1Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				"Station1_Step1_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, STATION1_BATCH_MAX_WAIT, station);
		this.station1Step5Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				"Station1_Step5_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, STATION1_BATCH_MAX_WAIT, station);

		return station;
	}

	private ToolGroup createStation2(final FabModel model) {
		final ToolGroup station = (ToolGroup) model.getSimComponentFactory().createToolGroup("Station2", 2,
				ProcessingType.LOT);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
				"Station2_PreventiveMaintenance",
				model.getValueObjectFactory().createConstantValueObject(STATION2_MAINTENANCE_DURATION),
				model.getValueObjectFactory().createConstantValueObject(STATION2_MAINTENANCE_INTERVAL), 
				station);

		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup(
				"Station2_UnscheduledBreakdown",
				model.getValueObjectFactory().createUniformValueObject(STATION2_REPAIR_MIN, STATION2_REPAIR_MAX),
				model.getValueObjectFactory().createUniformValueObject(STATION2_BREAKDOWN_MIN, STATION2_BREAKDOWN_MAX),
				station);

		return station;
	}

	private ToolGroup createStation3(final FabModel model) {
		final ToolGroup station = (ToolGroup) model.getSimComponentFactory().createToolGroup("Station3", 1,
				ProcessingType.LOT);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
				"Station3_PreventiveMaintenance",
				model.getValueObjectFactory().createConstantValueObject(STATION3_MAINTENANCE_DURATION),
				model.getValueObjectFactory().createConstantValueObject(STATION3_MAINTENANCE_INTERVAL), station);

		return station;
	}

	private void configureStation3Setups(final FabModel model) {
		this.station3SetupStates = new LinkedHashMap<>();
		for (final MiniFabProducts.ProductSpec product : MiniFabProducts.PRODUCTS) {
			addStation3SetupState(model, product.name, STEP_S3);
			addStation3SetupState(model, product.name, STEP_S6);
		}

		addStation3SetupTransitions(model);
	}

	private void addStation3SetupTransitions(final FabModel model) {
		// Station 3 is re-entrant, so every setup state needs an explicit change time.
		for (final SetupState currentState : this.station3SetupStates.values()) {
			for (final SetupState nextState : this.station3SetupStates.values()) {
				if (currentState != nextState) {
					model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(
							currentState, nextState, getStation3SetupTime(currentState, nextState), false, this.station3);
				}
			}
		}
	}

	private void addStation3SetupState(final FabModel model, final String productName, final String stepName) {
		final String setupName = station3SetupName(productName, stepName);
		this.station3SetupStates.put(setupName,
				model.getSimComponentFactory().createSetupStateAndAddToToolGroup(setupName, this.station3));
	}

	private long getStation3SetupTime(final SetupState currentState, final SetupState nextState) {
		final String[] currentParts = currentState.getSetupName().split("_");
		final String[] nextParts = nextState.getSetupName().split("_");
		final boolean sameLotType = currentParts[0].equals(nextParts[0]);
		final boolean sameStep = currentParts[1].equals(nextParts[1]);
		if (sameLotType && !sameStep) {
			return STATION3_SETUP_TIME_SAME_LOT_TYPE;
		}
		if (sameStep && !sameLotType) {
			return STATION3_SETUP_TIME_SAME_STEP;
		}
		return STATION3_SETUP_TIME_DEFAULT;
	}

	private SetupState getStation3SetupState(final String productName, final String stepName) {
		final SetupState setupState = this.station3SetupStates.get(station3SetupName(
				productName, stepName));
		if (setupState != null) {
			return setupState;
		}
		throw new IllegalArgumentException("Unsupported station 3 setup: " + productName + " " + stepName);
	}

	// ---------------------------------------------------------------------
	// Product line wiring
	// ---------------------------------------------------------------------

	private void createRecipesAndSources(final FabModel model, final Sink sink) {
		for (final MiniFabProducts.ProductSpec product : MiniFabProducts.PRODUCTS) {
			createProductLine(model, sink, product);
		}
	}

	private void createProductLine(final FabModel model, final Sink sink,
			final MiniFabProducts.ProductSpec productSpec) {
		final Recipe recipe = model.getSimComponentFactory().createRecipe(recipeName(productSpec.name));
		final MiniFabProducts.ProductProcessingTimes processingTimes = productSpec.processingTimes;
		final SetupState s3Setup = getStation3SetupState(productSpec.name, STEP_S3);
		final SetupState s6Setup = getStation3SetupState(productSpec.name, STEP_S6);

		addStation1Step(model, recipe, STEP_S1, processingTimes.s1, this.station1Step1Batch);
		addStation2Step(model, recipe, STEP_S2, processingTimes.s2);
		addStation3Step(model, recipe, STEP_S3, processingTimes.s3, s3Setup);
		addStation2Step(model, recipe, STEP_S4, processingTimes.s4);
		addStation1Step(model, recipe, STEP_S5, processingTimes.s5, this.station1Step5Batch);
		addStation3Step(model, recipe, STEP_S6, processingTimes.s6, s6Setup);
		addSinkStep(model, sink, recipe);

		final Product product = model.getSimComponentFactory().createProduct(productSpec.name, recipe);
		final ExponentialDuration interarrivalTime = model.getValueObjectFactory()
				.createExponentialValueObject(productSpec.interarrivalTimeMean);
		final LotSource source = (LotSource) model.getSimComponentFactory().createSource(
				sourceName(productSpec.name), product, interarrivalTime);
		source.setLotSize(LOT_SIZE);
		source.setDefaultPriority(productSpec.priority);
		source.setDueDateLeadTime(productSpec.dueDateLeadTime);
		source.setAllowSplit(false);
	}

	private void addStation1Step(final FabModel model, final Recipe recipe, final String stepName,
			final long meanProcessingTime, final BatchDetails batchDetails) {
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				stepName, this.station1, null, STATION1_LOAD, createProcessingTime(model, meanProcessingTime),
				STATION1_UNLOAD, batchDetails, null, ProcessType.LOT, recipe);
	}

	private void addStation2Step(final FabModel model, final Recipe recipe, final String stepName,
			final long meanProcessingTime) {
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				stepName, this.station2, null, STATION2_LOAD, createProcessingTime(model, meanProcessingTime),
				STATION2_UNLOAD, null, null, ProcessType.LOT, recipe);
	}

	private void addStation3Step(final FabModel model, final Recipe recipe, final String stepName,
			final long meanProcessingTime, final SetupState setupState) {
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				stepName, this.station3, null, STATION3_LOAD, createProcessingTime(model, meanProcessingTime),
				STATION3_UNLOAD, null, setupState, ProcessType.LOT, recipe);
	}

	private void addSinkStep(final FabModel model, final Sink sink, final Recipe recipe) {
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				STEP_SINK, sink, 0L, ProcessType.LOT, recipe);
	}

	private static IValue createProcessingTime(final FabModel model, final long meanProcessingTime) {
		final long variation = Math.round(meanProcessingTime * PROCESS_TIME_VARIATION);
		return model.getValueObjectFactory().createUniformValueObject(
				meanProcessingTime - variation, meanProcessingTime + variation);
	}
}
