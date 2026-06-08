package de.terministic.fabsim.metamodel.examples;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.terministic.fabsim.core.duration.ExponentialDuration;
import de.terministic.fabsim.metamodel.FabModel;
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
import de.terministic.fabsim.metamodel.externaldispatch.ExternalDispatchConfiguration;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;
import de.terministic.fabsim.metamodel.logging.LoggingDispatchRule;


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

	private static final int PA_PRIORITY = 1;
	private static final int PB_PRIORITY = 2;
	private static final int TW_PRIORITY = 3;
	private static final int PA_PRIORITY_WEIGHT = 10;
	private static final int PB_PRIORITY_WEIGHT = 5;
	private static final int TW_PRIORITY_WEIGHT = 1;
	private static final Map<Integer, Integer> PRIORITY_WEIGHTS = createPriorityWeights();

	public static final double FLOW_FACTOR = 3.5;
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

	public FabModel createMiniFabModel() {
		return createMiniFabModelWithExternalDispatch();
	}

	public FabModel createMiniFabModelWithExternalDispatch() {
		return createMiniFabModelWithExternalDispatch(ExternalDispatchConfiguration.localDefault(), null);
	}

	public FabModel createMiniFabModelWithExternalDispatch(final ExternalDispatchConfiguration configuration) {
		return createMiniFabModelWithExternalDispatch(configuration, null);
	}

	public FabModel createMiniFabModelWithExternalDispatch(final ExternalDispatchConfiguration configuration,
			final LocalLogWriter logWriter) {
		final ExternalDispatchRule externalDispatchRule = new ExternalDispatchRule("MiniFabExternalDispatch",
				configuration == null ? ExternalDispatchConfiguration.localDefault() : configuration);
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

	public static Map<Integer, Integer> getPriorityWeights() {
		return PRIORITY_WEIGHTS;
	}

	private FabModel createMiniFabModel(FabModel model, AbstractDispatchRule dispatchRule) {

		Sink sink = (Sink) model.getSimComponentFactory().createSink("Sink");

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

	private ToolGroup createStation1(FabModel model) {
		ToolGroup station = (ToolGroup) model.getSimComponentFactory().createToolGroup(
			"Station1", 2, ProcessingType.BATCH
		);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
			"Station1_PreventiveMaintenance",
			model.getValueObjectFactory().createConstantValueObject(STATION1_MAINTENANCE_DURATION),
			model.getValueObjectFactory().createConstantValueObject(STATION1_MAINTENANCE_INTERVAL),
			station
		);
			
		return station;
	}

	private ToolGroup createStation2(FabModel model) {
		ToolGroup station = (ToolGroup) model.getSimComponentFactory().createToolGroup("Station2", 2,
				ProcessingType.LOT);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
			"Station2_PreventiveMaintenance",
			model.getValueObjectFactory().createConstantValueObject(STATION2_MAINTENANCE_DURATION),
			model.getValueObjectFactory().createConstantValueObject(STATION2_MAINTENANCE_INTERVAL),
			station
		);

		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup("Station2_UnscheduledBreakdown",
			model.getValueObjectFactory().createUniformValueObject(STATION2_REPAIR_MIN, STATION2_REPAIR_MAX),
			model.getValueObjectFactory().createUniformValueObject(STATION2_BREAKDOWN_MIN, STATION2_BREAKDOWN_MAX),
			station
		);

		return station;
	}

	private ToolGroup createStation3(FabModel model) {
		ToolGroup station = (ToolGroup) model.getSimComponentFactory().createToolGroup("Station3", 1,
				ProcessingType.LOT);

		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
			"Station3_PreventiveMaintenance",
			model.getValueObjectFactory().createConstantValueObject(STATION3_MAINTENANCE_DURATION),
			model.getValueObjectFactory().createConstantValueObject(STATION3_MAINTENANCE_INTERVAL),
			station
		);
	
		return station;
	}

	private void configureStation3Setups(FabModel model) {
		this.paS3Setup = createStation3SetupState(model, "Pa_S3");
		this.paS6Setup = createStation3SetupState(model, "Pa_S6");
		this.pbS3Setup = createStation3SetupState(model, "Pb_S3");
		this.pbS6Setup = createStation3SetupState(model, "Pb_S6");
		this.twS3Setup = createStation3SetupState(model, "TW_S3");
		this.twS6Setup = createStation3SetupState(model, "TW_S6");

		addStation3SetupTransitions(model);
	}

	private void addStation3SetupTransitions(FabModel model) {
		final SetupState[] states = new SetupState[] {
				this.paS3Setup, this.paS6Setup, this.pbS3Setup, this.pbS6Setup, this.twS3Setup, this.twS6Setup
		};
		for (final SetupState currentState : states) {
			for (final SetupState nextState : states) {
				if (currentState != nextState) {
					model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(
							currentState, nextState, getStation3SetupTime(currentState, nextState), false, this.station3);
				}
			}
		}
	}

	private SetupState createStation3SetupState(FabModel model, String setupName) {
		return model.getSimComponentFactory().createSetupStateAndAddToToolGroup(setupName, this.station3);
	}

	private long getStation3SetupTime(final SetupState currentState, final SetupState nextState) {
		final String[] currentParts = currentState.getSetupName().split("_");
		final String[] nextParts = nextState.getSetupName().split("_");
		final boolean sameLotType = currentParts[0].equals(nextParts[0]);
		final boolean sameStep = currentParts[1].equals(nextParts[1]);
		if (sameLotType && !sameStep) {
			return 10L * MINUTE;
		}
		if (sameStep && !sameLotType) {
			return 5L * MINUTE;
		}
		return 12L * MINUTE;
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

	private void createRecipesAndSources(FabModel model, Sink sink) {
		createProductLine(model, sink, "Pa", "PaRecipe", 51L, PA_PRIORITY, DUE_DATE_LEAD_TIME);
		createProductLine(model, sink, "Pb", "PbRecipe", 30L, PB_PRIORITY, DUE_DATE_LEAD_TIME);
		createProductLine(model, sink, "TW", "TWRecipe", 3L, TW_PRIORITY, DUE_DATE_LEAD_TIME);
	}

	private static Map<Integer, Integer> createPriorityWeights() {
		final Map<Integer, Integer> weights = new LinkedHashMap<>();
		weights.put(PA_PRIORITY, PA_PRIORITY_WEIGHT);
		weights.put(PB_PRIORITY, PB_PRIORITY_WEIGHT);
		weights.put(TW_PRIORITY, TW_PRIORITY_WEIGHT);
		return Collections.unmodifiableMap(weights);
	}

	private void createProductLine(FabModel model, Sink sink, String productName, String recipeName, long weeklyLots,
			int priority, long dueDateLeadTime) {
		Recipe recipe = model.getSimComponentFactory().createRecipe(recipeName);
		final SetupState s3Setup = getStation3SetupState(productName, "S3");
		final SetupState s6Setup = getStation3SetupState(productName, "S6");
		final BatchDetails s1Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
			productName + "_Station1_Step1_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, this.station1
		);
		final BatchDetails s5Batch = model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
			productName + "_Station1_Step5_Batch", STATION1_BATCH_SIZE, STATION1_BATCH_SIZE, this.station1
		);

		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
			"S1", this.station1, null, STATION1_LOAD, STATION1_PROCESS_S1, STATION1_UNLOAD, s1Batch, null, ProcessType.LOT, recipe
		);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
			"S2", this.station2, null, STATION2_LOAD, STATION2_PROCESS_S2, STATION2_UNLOAD, null, null, ProcessType.LOT, recipe
		);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
			"S3", this.station3, null, STATION3_LOAD, STATION3_PROCESS_S3, STATION3_UNLOAD, null, s3Setup, ProcessType.LOT, recipe
		);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
			"S4", this.station2, null, STATION2_LOAD, STATION2_PROCESS_S4, STATION2_UNLOAD, null, null, ProcessType.LOT, recipe
		);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
			"S5", this.station1, null, STATION1_LOAD, STATION1_PROCESS_S5, STATION1_UNLOAD, s5Batch, null, ProcessType.LOT, recipe
		);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
			"S6", this.station3, null, STATION3_LOAD, STATION3_PROCESS_S6, STATION3_UNLOAD, null, s6Setup, ProcessType.LOT, recipe
		);
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Sink", sink, 0L, ProcessType.LOT, recipe);

		Product product = model.getSimComponentFactory().createProduct(productName, recipe);
		ExponentialDuration interarrivalTime = model.getValueObjectFactory().createExponentialValueObject(WEEK / weeklyLots);
		LotSource source = (LotSource) model.getSimComponentFactory().createSource(
			"Source_" + productName, product, interarrivalTime
		);
		source.setLotSize(LOT_SIZE);
		source.setDefaultPriority(priority);
		source.setDueDateLeadTime(dueDateLeadTime);
		source.setAllowSplit(false);
	}
}
