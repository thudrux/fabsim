package de.terministic.fabsim.metamodel.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.ExponentialDuration;
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
import de.terministic.fabsim.metamodel.components.equipment.breakdown.SimTimeBasedBreakdown;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.ExternalDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.FIFO;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;
import de.terministic.fabsim.metamodel.logging.LoggingDispatchRule;
import de.terministic.fabsim.metamodel.statistics.FinishedLotStatisticsCollector;


public class MiniFab {
	private static final long SECOND = 1000L;
	private static final long MINUTE = 60L * SECOND;
	private static final long HOUR = 60L * MINUTE;

	private static final int LOT_SIZE = 25;
	private static final int STATION1_BATCH_SIZE = LOT_SIZE * 3;

	private static final long STATION1_LOAD = 20L * MINUTE;
	private static final long STATION1_UNLOAD = 40L * MINUTE;
	private static final long STATION1_MAINTENANCE_DURATION = 75L * MINUTE;
	private static final long STATION1_MAINTENANCE_INTERVAL = 1440L * MINUTE;

	private static final long STATION2_LOAD = 15L * MINUTE;
	private static final long STATION2_UNLOAD = 15L * MINUTE;
	private static final long STATION2_MAINTENANCE_DURATION = 120L * MINUTE;
	private static final long STATION2_MAINTENANCE_INTERVAL = 720L * MINUTE;

	private static final long STATION3_LOAD = 10L * MINUTE;
	private static final long STATION3_UNLOAD = 10L * MINUTE;
	private static final long STATION3_MAINTENANCE_DURATION = 30L * MINUTE;
	private static final long STATION3_MAINTENANCE_INTERVAL = 720L * MINUTE;

	private static final long STATION2_BREAKDOWN_MIN = 1440L * MINUTE;
	private static final long STATION2_BREAKDOWN_MAX = 4560L * MINUTE;
	private static final long STATION2_REPAIR_MIN = 360L * MINUTE;
	private static final long STATION2_REPAIR_MAX = 480L * MINUTE;

	private static final long STATION3_SETUP_TIME_SAME_LOT_TYPE = 10L * MINUTE;
	private static final long STATION3_SETUP_TIME_SAME_STEP = 5L * MINUTE;
	private static final long STATION3_SETUP_TIME_DEFAULT = 12L * MINUTE;

	private static final int PA_PRIORITY = 1;
	private static final int PB_PRIORITY = 1;
	private static final int TW_PRIORITY = 1;
	private static final int PA_PRIORITY_WEIGHT = 1;
	private static final int PB_PRIORITY_WEIGHT = 1;
	private static final int TW_PRIORITY_WEIGHT = 1;
	private static final Map<Integer, Integer> PRIORITY_WEIGHTS = createPriorityWeights();

	private static final long PA_INTERARRIVAL_TIME = 225 * MINUTE;
	private static final long PB_INTERARRIVAL_TIME = 381 * MINUTE;
	private static final long TW_INTERARRIVAL_TIME = 4567 * MINUTE;

	public static final double FLOW_FACTOR = 2.5;

	private static final ProductProcessingTimes PA_PROCESS_TIMES = new ProductProcessingTimes(
			225L * MINUTE,
			18L * MINUTE,
			70L * MINUTE,
			34L * MINUTE,
			255L * MINUTE,
			16L * MINUTE);
	private static final ProductProcessingTimes PB_PROCESS_TIMES = new ProductProcessingTimes(
			225L * MINUTE,
			30L * MINUTE,
			55L * MINUTE,
			50L * MINUTE,
			255L * MINUTE,
			10L * MINUTE);
	private static final ProductProcessingTimes TW_PROCESS_TIMES = new ProductProcessingTimes(
			225L * MINUTE,
			42L * MINUTE,
			40L * MINUTE,
			66L * MINUTE,
			255L * MINUTE,
			4L * MINUTE);
	private static final long PA_DUE_DATE_LEAD_TIME = dueDateLeadTime(PA_PROCESS_TIMES);
	private static final long PB_DUE_DATE_LEAD_TIME = dueDateLeadTime(PB_PROCESS_TIMES);
	private static final long TW_DUE_DATE_LEAD_TIME = dueDateLeadTime(TW_PROCESS_TIMES);

	private ToolGroup station1;
	private ToolGroup station2;
	private ToolGroup station3;
	private SetupState paS3Setup;
	private SetupState paS6Setup;
	private SetupState pbS3Setup;
	private SetupState pbS6Setup;
	private SetupState twS3Setup;
	private SetupState twS6Setup;

	private static long dueDateLeadTime(final ProductProcessingTimes processingTimes) {
		return Math.round(processingTimes.getTotalProcessDuration() * FLOW_FACTOR);
	}

	// ---------------------------------------------------------------------
	// Public model factory API
	// ---------------------------------------------------------------------

	public FabModel createMiniFabModel() {
		return createMiniFabModelWithLocalDispatch(new FIFO());
	}

	public FabModel createMiniFabModelWithExternalDispatch(final DispatchProvider provider) {
		return createMiniFabModelWithExternalDispatch(provider, null);
	}

	public FabModel createMiniFabModelWithExternalDispatch(final DispatchProvider provider,
			final LocalLogWriter logWriter) {
		final ExternalDispatchRule externalDispatchRule = new ExternalDispatchRule("MiniFabExternalDispatch",
				provider);
		return createMiniFabModelWithDispatchRule(externalDispatchRule, logWriter);
	}

	public FabModel createMiniFabModelWithLocalDispatch(final AbstractDispatchRule dispatchRule) {
		return createMiniFabModelWithDispatchRule(dispatchRule, null);
	}

	public FabModel createMiniFabModelWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final LocalLogWriter logWriter) {
		return createMiniFabModelWithDispatchRule(dispatchRule, logWriter);
	}

	public FabModel createMiniFabModelWithDispatchRule(final AbstractDispatchRule dispatchRule,
			final LocalLogWriter logWriter) {
		if (dispatchRule == null) {
			throw new IllegalArgumentException("dispatchRule must not be null");
		}
		FabModel model = new FabModel();
		final AbstractDispatchRule effectiveRule;
		if (dispatchRule instanceof LoggingDispatchRule) {
			effectiveRule = dispatchRule;
		} else if (logWriter != null) {
			effectiveRule = new LoggingDispatchRule(dispatchRule, logWriter);
		} else {
			effectiveRule = dispatchRule;
		}
		return createMiniFabModel(model, effectiveRule);
	}

	// ---------------------------------------------------------------------
	// Public simulation API
	// ---------------------------------------------------------------------

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, 0L, null);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final LocalLogWriter logWriter) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, 1, 0L, logWriter);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, runs, 0L, null);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs, final LocalLogWriter logWriter) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, runs, 0L, logWriter);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final long warmupTimeHours) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, 1, warmupTimeHours, null);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final long warmupTimeHours, final LocalLogWriter logWriter) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, 1, warmupTimeHours, logWriter);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs, final long warmupTimeHours) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, runs, warmupTimeHours, null);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs, final long warmupTimeHours,
			final LocalLogWriter logWriter) {
		return runMiniFabWithDispatchRule(dispatchRule, simulationTimeHours, runs, warmupTimeHours, logWriter);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, 1, 0L, null);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final LocalLogWriter logWriter) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, 1, 0L, logWriter);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final int runs) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, runs, 0L, null);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final int runs, final LocalLogWriter logWriter) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, runs, 0L, logWriter);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final long warmupTimeHours) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, 1, warmupTimeHours, null);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final long warmupTimeHours, final LocalLogWriter logWriter) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, 1, warmupTimeHours, logWriter);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final int runs, final long warmupTimeHours) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, runs, warmupTimeHours, null);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final int runs, final long warmupTimeHours,
			final LocalLogWriter logWriter) {
		if (runs <= 0) {
			throw new IllegalArgumentException("runs must be a positive number");
		}
		if (runs > 1 && logWriter != null) {
			throw new IllegalArgumentException("Logging is only supported for a single MiniFab run");
		}
		validateTiming(simulationTimeHours, warmupTimeHours);

		final List<MiniFabRunResult> runResults = new ArrayList<>(runs);
		for (int run = 0; run < runs; run++) {
			final LocalLogWriter effectiveLogWriter = runs == 1 ? logWriter : null;
			final FabModel model = createMiniFabModelWithExternalDispatch(provider, effectiveLogWriter);
			runResults.add(runSimulation(model, simulationTimeHours, warmupTimeHours));
		}
		return aggregateRunResults(runs, simulationTimeHours, runResults);
	}

	// ---------------------------------------------------------------------
	// Internal simulation orchestration
	// ---------------------------------------------------------------------

	private MiniFabRunResult runMiniFabWithDispatchRule(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs, final long warmupTimeHours,
			final LocalLogWriter logWriter) {
		if (runs <= 0) {
			throw new IllegalArgumentException("runs must be a positive number");
		}
		if (runs > 1 && (logWriter != null || dispatchRule instanceof LoggingDispatchRule)) {
			throw new IllegalArgumentException("Logging is only supported for a single MiniFab run");
		}
		validateTiming(simulationTimeHours, warmupTimeHours);

		final List<MiniFabRunResult> runResults = new ArrayList<>(runs);
		for (int run = 0; run < runs; run++) {
			final LocalLogWriter effectiveLogWriter = runs == 1 ? logWriter : null;
			final FabModel model = createMiniFabModelWithDispatchRule(dispatchRule, effectiveLogWriter);
			runResults.add(runSimulation(model, simulationTimeHours, warmupTimeHours));
		}
		return aggregateRunResults(runs, simulationTimeHours, runResults);
	}

	// ---------------------------------------------------------------------
	// Result aggregation and statistics
	// ---------------------------------------------------------------------

	private MiniFabRunResult aggregateRunResults(final int runs, final long simulationTimeHours,
			final List<MiniFabRunResult> runResults) {
		final SummaryStatistics completedWafersPerDayStatistics = summarize(runResults,
				Metric.COMPLETED_WAFERS_PER_DAY);
		final SummaryStatistics tardinessStatistics = summarize(runResults, Metric.TARDINESS_PER_WAFER_MINUTES);
		final SummaryStatistics completedWafersStatistics = summarize(runResults, Metric.COMPLETED_WAFERS);
		final SummaryStatistics tardyWafersStatistics = summarize(runResults, Metric.TARDY_WAFERS);
		final SummaryStatistics flowFactorStatistics = summarize(runResults, Metric.FLOW_FACTOR);
		return new MiniFabRunResult(runs, simulationTimeHours,
				completedWafersPerDayStatistics.mean, completedWafersPerDayStatistics.stdDev,
				tardinessStatistics.mean, tardinessStatistics.stdDev,
				completedWafersStatistics.mean, completedWafersStatistics.stdDev, tardyWafersStatistics.mean,
				tardyWafersStatistics.stdDev, flowFactorStatistics.mean, flowFactorStatistics.stdDev);
	}

	private SummaryStatistics summarize(final List<MiniFabRunResult> runResults, final Metric metric) {
		final double[] values = new double[runResults.size()];
		for (int i = 0; i < runResults.size(); i++) {
			final MiniFabRunResult result = runResults.get(i);
			switch (metric) {
			case COMPLETED_WAFERS_PER_DAY:
				values[i] = result.getCompletedWafersPerDay();
				break;
			case TARDINESS_PER_WAFER_MINUTES:
				values[i] = result.getTardinessPerWaferMinutes();
				break;
			case COMPLETED_WAFERS:
				values[i] = result.getCompletedWafers();
				break;
			case TARDY_WAFERS:
				values[i] = result.getTardyWafers();
				break;
			case FLOW_FACTOR:
				values[i] = result.getFlowFactor();
				break;
			default:
				throw new IllegalArgumentException("Unsupported metric: " + metric);
			}
		}
		return summarize(values);
	}

	private SummaryStatistics summarize(final double[] values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("values must not be empty");
		}
		double sum = 0.0d;
		for (final double value : values) {
			sum += value;
		}
		final double mean = sum / values.length;
		double variance = 0.0d;
		if (values.length > 1) {
			for (final double value : values) {
				final double delta = value - mean;
				variance += delta * delta;
			}
			variance = variance / (values.length - 1);
		}
		return new SummaryStatistics(mean, Math.sqrt(Math.max(0.0d, variance)));
	}

	private static final class SummaryStatistics {
		private final double mean;
		private final double stdDev;

		private SummaryStatistics(final double mean, final double stdDev) {
			this.mean = mean;
			this.stdDev = stdDev;
		}
	}

	private enum Metric {
		COMPLETED_WAFERS_PER_DAY,
		TARDINESS_PER_WAFER_MINUTES,
		COMPLETED_WAFERS,
		TARDY_WAFERS,
		FLOW_FACTOR
	}

	// ---------------------------------------------------------------------
	// Simulation execution
	// ---------------------------------------------------------------------

	private MiniFabRunResult runSimulation(final FabModel model, final long simulationTimeHours,
			final long warmupTimeHours) {
		final SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		final long simulationTimeMillis = Math.multiplyExact(simulationTimeHours, HOUR);
		final long warmupTimeMillis = Math.multiplyExact(warmupTimeHours, HOUR);
		final long measurementTimeHours = Math.subtractExact(simulationTimeHours, warmupTimeHours);
		final FinishedLotStatisticsCollector finishedLotStatisticsCollector = new FinishedLotStatisticsCollector(model,
				PRIORITY_WEIGHTS, warmupTimeMillis);
		engine.addListener(finishedLotStatisticsCollector);
		engine.runSimulation(simulationTimeMillis);
		return new MiniFabRunResult(simulationTimeHours, measurementTimeHours,
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

	private FabModel createMiniFabModel(final FabModel model, final AbstractDispatchRule dispatchRule) {
		final Sink sink = (Sink) model.getSimComponentFactory().createSink("Sink");

		this.station1 = createStation1(model);
		this.station2 = createStation2(model);
		this.station3 = createStation3(model);
		this.station1.setDispatchRule(dispatchRule);
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

		return station;
	}

	private ToolGroup createStation2(final FabModel model) {
		final ToolGroup station = (ToolGroup) model.getSimComponentFactory().createToolGroup("Station2", 2,
				ProcessingType.LOT);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
				"Station2_PreventiveMaintenance",
				model.getValueObjectFactory().createConstantValueObject(STATION2_MAINTENANCE_DURATION),
				model.getValueObjectFactory().createConstantValueObject(STATION2_MAINTENANCE_INTERVAL), station);

		final SimTimeBasedBreakdown breakdown = model.getSimComponentFactory()
				.createSimulationTimeBasedBreakdownAndAddToToolGroup("Station2_UnscheduledBreakdown",
				model.getValueObjectFactory().createUniformValueObject(STATION2_REPAIR_MIN, STATION2_REPAIR_MAX),
				model.getValueObjectFactory().createUniformValueObject(STATION2_BREAKDOWN_MIN, STATION2_BREAKDOWN_MAX),
				station);
		breakdown.setScrapsInProcessItems(true);

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
		this.paS3Setup = createStation3SetupState(model, "Pa_S3");
		this.paS6Setup = createStation3SetupState(model, "Pa_S6");
		this.pbS3Setup = createStation3SetupState(model, "Pb_S3");
		this.pbS6Setup = createStation3SetupState(model, "Pb_S6");
		this.twS3Setup = createStation3SetupState(model, "TW_S3");
		this.twS6Setup = createStation3SetupState(model, "TW_S6");

		addStation3SetupTransitions(model);
	}

	private void addStation3SetupTransitions(final FabModel model) {
		// Station 3 is re-entrant, so every setup state needs an explicit change time.
		final SetupState[] states = new SetupState[] { this.paS3Setup, this.paS6Setup, this.pbS3Setup, this.pbS6Setup,
				this.twS3Setup, this.twS6Setup };
		for (final SetupState currentState : states) {
			for (final SetupState nextState : states) {
				if (currentState != nextState) {
					model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(
							currentState, nextState, getStation3SetupTime(currentState, nextState), false, this.station3);
				}
			}
		}
	}

	private SetupState createStation3SetupState(final FabModel model, final String setupName) {
		return model.getSimComponentFactory().createSetupStateAndAddToToolGroup(setupName, this.station3);
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
		if ("Pa".equals(productName)) {
			return "S3".equals(stepName) ? this.paS3Setup : this.paS6Setup;
		}
		if ("Pb".equals(productName)) {
			return "S3".equals(stepName) ? this.pbS3Setup : this.pbS6Setup;
		}
		if ("TW".equals(productName)) {
			return "S3".equals(stepName) ? this.twS3Setup : this.twS6Setup;
		}
		throw new IllegalArgumentException("Unsupported product name for station 3 setup: " + productName);
	}

	// ---------------------------------------------------------------------
	// Product line wiring
	// ---------------------------------------------------------------------

	private void createRecipesAndSources(final FabModel model, final Sink sink) {
		createProductLine(model, sink, "Pa", "PaRecipe", PA_INTERARRIVAL_TIME, PA_PRIORITY,
				PA_DUE_DATE_LEAD_TIME, PA_PROCESS_TIMES);
		createProductLine(model, sink, "Pb", "PbRecipe", PB_INTERARRIVAL_TIME, PB_PRIORITY,
				PB_DUE_DATE_LEAD_TIME, PB_PROCESS_TIMES);
		createProductLine(model, sink, "TW", "TWRecipe", TW_INTERARRIVAL_TIME, TW_PRIORITY,
				TW_DUE_DATE_LEAD_TIME, TW_PROCESS_TIMES);
	}

	private static Map<Integer, Integer> createPriorityWeights() {
		final Map<Integer, Integer> weights = new LinkedHashMap<>();
		weights.put(PA_PRIORITY, PA_PRIORITY_WEIGHT);
		weights.put(PB_PRIORITY, PB_PRIORITY_WEIGHT);
		weights.put(TW_PRIORITY, TW_PRIORITY_WEIGHT);
		return Collections.unmodifiableMap(weights);
	}

	private void createProductLine(final FabModel model, final Sink sink, final String productName,
			final String recipeName, final long interarrivalTimeMean, final int priority, final long dueDateLeadTime,
			final ProductProcessingTimes processingTimes) {
		final Recipe recipe = model.getSimComponentFactory().createRecipe(recipeName);
		final SetupState s3Setup = getStation3SetupState(productName, "S3");
		final SetupState s6Setup = getStation3SetupState(productName, "S6");
		final BatchDetails s1Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				productName + "_Station1_Step1_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, this.station1);
		final BatchDetails s5Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				productName + "_Station1_Step5_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, this.station1);

		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S1", this.station1, null, STATION1_LOAD, processingTimes.s1, STATION1_UNLOAD, s1Batch, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S2", this.station2, null, STATION2_LOAD, processingTimes.s2, STATION2_UNLOAD, null, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S3", this.station3, null, STATION3_LOAD, processingTimes.s3, STATION3_UNLOAD, null, s3Setup,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S4", this.station2, null, STATION2_LOAD, processingTimes.s4, STATION2_UNLOAD, null, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S5", this.station1, null, STATION1_LOAD, processingTimes.s5, STATION1_UNLOAD, s5Batch, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S6", this.station3, null, STATION3_LOAD, processingTimes.s6, STATION3_UNLOAD, null, s6Setup,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink", sink, 0L, ProcessType.LOT, recipe);

		final Product product = model.getSimComponentFactory().createProduct(productName, recipe);
		final ExponentialDuration interarrivalTime = model.getValueObjectFactory()
				.createExponentialValueObject(interarrivalTimeMean);
		final LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source_" + productName,
				product, interarrivalTime);
		source.setLotSize(LOT_SIZE);
		source.setDefaultPriority(priority);
		source.setDueDateLeadTime(dueDateLeadTime);
		source.setAllowSplit(false);
	}

	private static final class ProductProcessingTimes {
		private final long s1;
		private final long s2;
		private final long s3;
		private final long s4;
		private final long s5;
		private final long s6;

		private ProductProcessingTimes(final long s1, final long s2, final long s3, final long s4,
				final long s5, final long s6) {
			this.s1 = s1;
			this.s2 = s2;
			this.s3 = s3;
			this.s4 = s4;
			this.s5 = s5;
			this.s6 = s6;
		}

		private long getTotalProcessDuration() {
			return this.s1 + this.s2 + this.s3 + this.s4 + this.s5 + this.s6;
		}
	}
}
