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
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.ExternalDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.FIFO;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;
import de.terministic.fabsim.metamodel.logging.LoggingDispatchRule;
import de.terministic.fabsim.metamodel.statistics.FinishedLotStatisticsCollector;


/**
 * This model is based on the MiniFab environment from researchers from Arizona State
 * University and Intel. A detailed description can be found in
 * I. A. El-Khouly, K. S. El-Kilany and A. E. El-Sayed, "Modelling and
 * simulation of re-entrant flow shop scheduling: An application in
 * semiconductor manufacturing," 2009 International Conference on Computers &
 * Industrial Engineering, Troyes, France, 2009, pp. 211-216,
 * DOI: 10.1109/ICCIE.2009.5223754.
 */
public class MiniFab {
	private static final long SECOND = 1000L;
	private static final long MINUTE = 60L * SECOND;
	private static final long HOUR = 60L * MINUTE;
	private static final long DAY = 24L * HOUR;
	private static final long WEEK = 7L * DAY;

	private static final int LOT_SIZE = 24;
	private static final int STATION1_BATCH_SIZE = LOT_SIZE * 3;

	private static final long STATION1_LOAD = 20L * MINUTE;
	private static final long STATION1_PROCESS_S1 = 225L * MINUTE;
	private static final long STATION1_PROCESS_S5 = 255L * MINUTE;
	private static final long STATION1_UNLOAD = 40L * MINUTE;
	private static final long STATION1_MAINTENANCE_DURATION = 75L * MINUTE;
	private static final long STATION1_MAINTENANCE_INTERVAL = 24L * HOUR;

	private static final long STATION2_LOAD = 15L * MINUTE;
	private static final long STATION2_PROCESS_S2 = 30L * MINUTE;
	private static final long STATION2_PROCESS_S4 = 50L * MINUTE;
	private static final long STATION2_UNLOAD = 15L * MINUTE;
	private static final long STATION2_MAINTENANCE_DURATION = 120L * MINUTE;
	private static final long STATION2_MAINTENANCE_INTERVAL = 12L * HOUR;

	private static final long STATION3_LOAD = 10L * MINUTE;
	private static final long STATION3_PROCESS_S3 = 55L * MINUTE;
	private static final long STATION3_PROCESS_S6 = 10L * MINUTE;
	private static final long STATION3_UNLOAD = 10L * MINUTE;
	private static final long STATION3_MAINTENANCE_DURATION = 30L * MINUTE;
	private static final long STATION3_MAINTENANCE_INTERVAL = 12L * HOUR;

	private static final long STATION2_BREAKDOWN_MIN = 24L * HOUR;
	private static final long STATION2_BREAKDOWN_MAX = 76L * HOUR;
	private static final long STATION2_REPAIR_MIN = 6L * HOUR;
	private static final long STATION2_REPAIR_MAX = 8L * HOUR;

	private static final long STATION3_SETUP_TIME_SAME_LOT_TYPE = 10L * MINUTE;
	private static final long STATION3_SETUP_TIME_SAME_STEP = 5L * MINUTE;
	private static final long STATION3_SETUP_TIME_DEFAULT = 12L * MINUTE;

	private static final int PA_PRIORITY = 1;
	private static final int PB_PRIORITY = 2;
	private static final int TW_PRIORITY = 3;
	private static final int PA_PRIORITY_WEIGHT = 10;
	private static final int PB_PRIORITY_WEIGHT = 5;
	private static final int TW_PRIORITY_WEIGHT = 1;
	private static final Map<Integer, Integer> PRIORITY_WEIGHTS = createPriorityWeights();

	private static final long PA_WEEKLY_LOTS = 51L;
	private static final long PB_WEEKLY_LOTS = 30L;
	private static final long TW_WEEKLY_LOTS = 3L;

	public static final double FLOW_FACTOR = 3.0;
	private static final long DUE_DATE_LEAD_TIME = Math.round(
		(
			STATION1_PROCESS_S1 
			+ STATION2_PROCESS_S2 
			+ STATION3_PROCESS_S3 
			+ STATION2_PROCESS_S4
			+ STATION1_PROCESS_S5 
			+ STATION3_PROCESS_S6
		) * FLOW_FACTOR
	);

	private ToolGroup station1;
	private ToolGroup station2;
	private ToolGroup station3;
	private SetupState paS3Setup;
	private SetupState paS6Setup;
	private SetupState pbS3Setup;
	private SetupState pbS6Setup;
	private SetupState twS3Setup;
	private SetupState twS6Setup;

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
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, null);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final LocalLogWriter logWriter) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, 1, logWriter);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs) {
		return runMiniFabWithLocalDispatch(dispatchRule, simulationTimeHours, runs, null);
	}

	public MiniFabRunResult runMiniFabWithLocalDispatch(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs, final LocalLogWriter logWriter) {
		return runMiniFabWithDispatchRule(dispatchRule, simulationTimeHours, runs, logWriter);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, 1, null);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final LocalLogWriter logWriter) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, 1, logWriter);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final int runs) {
		return runMiniFabWithExternalDispatch(provider, simulationTimeHours, runs, null);
	}

	public MiniFabRunResult runMiniFabWithExternalDispatch(final DispatchProvider provider,
			final long simulationTimeHours, final int runs, final LocalLogWriter logWriter) {
		if (runs <= 0) {
			throw new IllegalArgumentException("runs must be a positive number");
		}
		if (runs > 1 && logWriter != null) {
			throw new IllegalArgumentException("Logging is only supported for a single MiniFab run");
		}

		final List<MiniFabRunResult> runResults = new ArrayList<>(runs);
		for (int run = 0; run < runs; run++) {
			final LocalLogWriter effectiveLogWriter = runs == 1 ? logWriter : null;
			final FabModel model = createMiniFabModelWithExternalDispatch(provider, effectiveLogWriter);
			runResults.add(runSimulation(model, simulationTimeHours));
		}
		return aggregateRunResults(runs, simulationTimeHours, runResults);
	}

	// ---------------------------------------------------------------------
	// Internal simulation orchestration
	// ---------------------------------------------------------------------

	private MiniFabRunResult runMiniFabWithDispatchRule(final AbstractDispatchRule dispatchRule,
			final long simulationTimeHours, final int runs, final LocalLogWriter logWriter) {
		if (runs <= 0) {
			throw new IllegalArgumentException("runs must be a positive number");
		}
		if (runs > 1 && (logWriter != null || dispatchRule instanceof LoggingDispatchRule)) {
			throw new IllegalArgumentException("Logging is only supported for a single MiniFab run");
		}

		final List<MiniFabRunResult> runResults = new ArrayList<>(runs);
		for (int run = 0; run < runs; run++) {
			final LocalLogWriter effectiveLogWriter = runs == 1 ? logWriter : null;
			final FabModel model = createMiniFabModelWithDispatchRule(dispatchRule, effectiveLogWriter);
			runResults.add(runSimulation(model, simulationTimeHours));
		}
		return aggregateRunResults(runs, simulationTimeHours, runResults);
	}

	// ---------------------------------------------------------------------
	// Result aggregation and statistics
	// ---------------------------------------------------------------------

	private MiniFabRunResult aggregateRunResults(final int runs, final long simulationTimeHours,
			final List<MiniFabRunResult> runResults) {
		final SummaryStatistics throughputStatistics = summarize(runResults, Metric.THROUGHPUT);
		final SummaryStatistics tardyWafersStatistics = summarize(runResults, Metric.TARDY_WAFERS);
		final SummaryStatistics totalWeightedTardinessStatistics = summarize(runResults,
				Metric.TOTAL_WEIGHTED_TARDINESS);
		return new MiniFabRunResult(runs, simulationTimeHours, simulationTimeHours * (double) HOUR,
				throughputStatistics.mean, throughputStatistics.stdDev, tardyWafersStatistics.mean,
				tardyWafersStatistics.stdDev, totalWeightedTardinessStatistics.mean,
				totalWeightedTardinessStatistics.stdDev);
	}

	private SummaryStatistics summarize(final List<MiniFabRunResult> runResults, final Metric metric) {
		final double[] values = new double[runResults.size()];
		for (int i = 0; i < runResults.size(); i++) {
			final MiniFabRunResult result = runResults.get(i);
			switch (metric) {
			case THROUGHPUT:
				values[i] = result.getFinishedWafers();
				break;
			case TARDY_WAFERS:
				values[i] = result.getTardyWafers();
				break;
			case TOTAL_WEIGHTED_TARDINESS:
				values[i] = result.getTotalWeightedTardiness();
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
		THROUGHPUT,
		TARDY_WAFERS,
		TOTAL_WEIGHTED_TARDINESS
	}

	// ---------------------------------------------------------------------
	// Simulation execution
	// ---------------------------------------------------------------------

	private MiniFabRunResult runSimulation(final FabModel model, final long simulationTimeHours) {
		final SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		final long simulationTimeMillis = Math.multiplyExact(simulationTimeHours, HOUR);
		final FinishedLotStatisticsCollector finishedLotStatisticsCollector = new FinishedLotStatisticsCollector(model,
				PRIORITY_WEIGHTS);
		engine.addListener(finishedLotStatisticsCollector);
		engine.runSimulation(simulationTimeMillis);
		return new MiniFabRunResult(simulationTimeHours, simulationTimeMillis,
				finishedLotStatisticsCollector.getFinishedWafers(),
				finishedLotStatisticsCollector.getTardyWafers(),
				finishedLotStatisticsCollector.getTotalWeightedTardiness());
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
		createProductLine(model, sink, "Pa", "PaRecipe", PA_WEEKLY_LOTS, PA_PRIORITY, DUE_DATE_LEAD_TIME);
		createProductLine(model, sink, "Pb", "PbRecipe", PB_WEEKLY_LOTS, PB_PRIORITY, DUE_DATE_LEAD_TIME);
		createProductLine(model, sink, "TW", "TWRecipe", TW_WEEKLY_LOTS, TW_PRIORITY, DUE_DATE_LEAD_TIME);
	}

	private static Map<Integer, Integer> createPriorityWeights() {
		final Map<Integer, Integer> weights = new LinkedHashMap<>();
		weights.put(PA_PRIORITY, PA_PRIORITY_WEIGHT);
		weights.put(PB_PRIORITY, PB_PRIORITY_WEIGHT);
		weights.put(TW_PRIORITY, TW_PRIORITY_WEIGHT);
		return Collections.unmodifiableMap(weights);
	}

	private void createProductLine(final FabModel model, final Sink sink, final String productName,
			final String recipeName, final long weeklyLots, final int priority, final long dueDateLeadTime) {
		final Recipe recipe = model.getSimComponentFactory().createRecipe(recipeName);
		final SetupState s3Setup = getStation3SetupState(productName, "S3");
		final SetupState s6Setup = getStation3SetupState(productName, "S6");
		final BatchDetails s1Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				productName + "_Station1_Step1_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, this.station1);
		final BatchDetails s5Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				productName + "_Station1_Step5_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, this.station1);

		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S1", this.station1, null, STATION1_LOAD, STATION1_PROCESS_S1, STATION1_UNLOAD, s1Batch, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S2", this.station2, null, STATION2_LOAD, STATION2_PROCESS_S2, STATION2_UNLOAD, null, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S3", this.station3, null, STATION3_LOAD, STATION3_PROCESS_S3, STATION3_UNLOAD, null, s3Setup,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S4", this.station2, null, STATION2_LOAD, STATION2_PROCESS_S4, STATION2_UNLOAD, null, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S5", this.station1, null, STATION1_LOAD, STATION1_PROCESS_S5, STATION1_UNLOAD, s5Batch, null,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"S6", this.station3, null, STATION3_LOAD, STATION3_PROCESS_S6, STATION3_UNLOAD, null, s6Setup,
				ProcessType.LOT, recipe);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink", sink, 0L, ProcessType.LOT, recipe);

		final Product product = model.getSimComponentFactory().createProduct(productName, recipe);
		final ExponentialDuration interarrivalTime = model.getValueObjectFactory()
				.createExponentialValueObject(WEEK / weeklyLots);
		final LotSource source = (LotSource) model.getSimComponentFactory().createSource("Source_" + productName,
				product, interarrivalTime);
		source.setLotSize(LOT_SIZE);
		source.setDefaultPriority(priority);
		source.setDueDateLeadTime(dueDateLeadTime);
		source.setAllowSplit(false);
	}
}
