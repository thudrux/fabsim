package de.terministic.fabsim.benchmarks.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.benchmarks.core.results.RunResult;
import de.terministic.fabsim.benchmarks.core.setup.SetupManager;
import de.terministic.fabsim.benchmarks.core.specs.BreakdownSpec;
import de.terministic.fabsim.benchmarks.core.specs.FabSpec;
import de.terministic.fabsim.benchmarks.core.specs.MaintenanceSpec;
import de.terministic.fabsim.benchmarks.core.specs.ProductSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteStepSpec;
import de.terministic.fabsim.benchmarks.core.specs.ToolGroupSpec;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.ExternalDispatchRule;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;
import de.terministic.fabsim.metamodel.logging.LoggingDispatchRule;
import de.terministic.fabsim.metamodel.statistics.FinishedLotStatisticsCollector;

public abstract class BenchmarkFab {
	public static final long SECOND = 1000L;
	public static final long MINUTE = 60L * SECOND;
	public static final long HOUR = 60L * MINUTE;
	protected static final String STEP_SINK = "Sink";
	protected static final int LOT_SIZE = 25;

	private DispatchMode dispatchMode;
	private AbstractDispatchRule dispatchRule;
	private DispatchProvider dispatchProvider;
	private LocalLogWriter logWriter;

	private enum DispatchMode {
		LOCAL,
		EXTERNAL
	}

	protected BenchmarkFab(final DispatchProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider must not be null");
		}
		this.dispatchMode = DispatchMode.EXTERNAL;
		this.dispatchProvider = provider;
		this.dispatchRule = null;
		this.logWriter = null;
	}

	protected BenchmarkFab(final AbstractDispatchRule dispatchRule,
			final LocalLogWriter logWriter) {
		if (dispatchRule == null) {
			throw new IllegalArgumentException("dispatchRule must not be null");
		}
		this.dispatchMode = DispatchMode.LOCAL;
		this.dispatchRule = dispatchRule;
		this.dispatchProvider = null;
		this.logWriter = logWriter;
	}

	public final RunResult run(final long simulationTimeHours, final long warmupTimeHours, final long seed) {
		if (simulationTimeHours <= 0L) {
			throw new IllegalArgumentException("simulationTimeHours must be a positive number");
		}
		if (warmupTimeHours < 0L) {
			throw new IllegalArgumentException("warmupTimeHours must not be negative");
		}
		if (warmupTimeHours >= simulationTimeHours) {
			throw new IllegalArgumentException("warmupTimeHours must be less than simulationTimeHours");
		}

		return runSimulation(createFabModel(seed), simulationTimeHours, warmupTimeHours);
	}

	private FabModel createFabModel(final long seed) {
		final AbstractDispatchRule effectiveRule;
		switch (this.dispatchMode) {
			case EXTERNAL:
				effectiveRule = new ExternalDispatchRule(getBenchmarkName() + "ExternalDispatch",
						this.dispatchProvider, getFlowFactor());
				break;
			case LOCAL:
				if (this.dispatchRule instanceof LoggingDispatchRule || this.logWriter == null) {
					effectiveRule = this.dispatchRule;
				} else {
					effectiveRule = new LoggingDispatchRule(this.dispatchRule, this.logWriter, getFlowFactor());
				}
				break;
			default:
				throw new IllegalStateException(getBenchmarkName() + " must be built before creating a model");
		}
		final FabModel fabModel = new FabModel(seed);
		return assembleFabModel(fabModel, effectiveRule, createFabSpec(fabModel));
	}

	private RunResult runSimulation(final FabModel model, final long simulationTimeHours,
			final long warmupTimeHours) {
		final SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		final long simulationTimeMillis = Math.multiplyExact(simulationTimeHours, HOUR);
		final long warmupTimeMillis = Math.multiplyExact(warmupTimeHours, HOUR);
		final long measurementTimeHours = Math.subtractExact(simulationTimeHours, warmupTimeHours);
		final FinishedLotStatisticsCollector finishedLotStatisticsCollector = new FinishedLotStatisticsCollector(model,
				warmupTimeMillis);
		engine.addListener(finishedLotStatisticsCollector);
		engine.runSimulation(simulationTimeMillis);
		return new RunResult(simulationTimeHours, measurementTimeHours,
				finishedLotStatisticsCollector.getFinishedWafers(),
				finishedLotStatisticsCollector.getTardyWafers(),
				finishedLotStatisticsCollector.getTardinessPerWaferMinutes(),
				finishedLotStatisticsCollector.getFlowFactorMean());
	}

	private FabModel assembleFabModel(final FabModel model, final AbstractDispatchRule dispatchRule,
			final FabSpec fabSpec) {
		final Sink sink = (Sink) model.getSimComponentFactory().createSink(fabSpec.getSinkName());
		final Map<String, ToolGroup> toolGroups = createToolGroups(model, dispatchRule, fabSpec);
		final Map<String, ToolGroupSpec> toolGroupSpecs = getToolGroupSpecs(fabSpec);
		final SetupManager setupManager = new SetupManager(model, fabSpec, toolGroups);
		for (final ProductSpec productSpec : fabSpec.getProducts()) {
			createProductLine(model, productSpec, sink, toolGroups, toolGroupSpecs, setupManager, fabSpec);
		}
		return model;
	}

	private Map<String, ToolGroupSpec> getToolGroupSpecs(final FabSpec fabSpec) {
		final LinkedHashMap<String, ToolGroupSpec> toolGroupSpecs = new LinkedHashMap<>();
		for (final ToolGroupSpec toolGroupSpec : fabSpec.getToolGroups()) {
			toolGroupSpecs.put(toolGroupSpec.getName(), toolGroupSpec);
		}
		return toolGroupSpecs;
	}

	protected Map<String, ToolGroup> createToolGroups(final FabModel model, final AbstractDispatchRule dispatchRule,
			final FabSpec fabSpec) {
		final LinkedHashMap<String, ToolGroup> toolGroups = new LinkedHashMap<>();
		for (final ToolGroupSpec toolGroupSpec : fabSpec.getToolGroups()) {
			if (toolGroups.containsKey(toolGroupSpec.getName())) {
				throw new IllegalArgumentException("Duplicate toolgroup name: " + toolGroupSpec.getName());
			}
			final ToolGroup toolGroup = createToolGroup(model, dispatchRule, toolGroupSpec);
			toolGroups.put(toolGroupSpec.getName(), toolGroup);
		}
		return toolGroups;
	}

	protected ToolGroup createToolGroup(final FabModel model, final AbstractDispatchRule dispatchRule,
			final ToolGroupSpec toolGroupSpec) {
		final ToolGroup toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup(
				toolGroupSpec.getName(), toolGroupSpec.getNumberOfTools(), toolGroupSpec.getProcessingType());
		toolGroup.setDispatchRule(dispatchRule);
		createPredictiveMaintenance(model, toolGroup, toolGroupSpec);
		createBreakdown(model, toolGroup, toolGroupSpec);
		return toolGroup;
	}

	private void createPredictiveMaintenance(final FabModel model, final ToolGroup toolGroup,
			final ToolGroupSpec toolGroupSpec) {
		final MaintenanceSpec maintenance = toolGroupSpec.getMaintenance();
		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
				toolGroupSpec.getName() + "_PredictiveMaintenance", maintenance.getDurationDistribution(),
				model.getValueObjectFactory().createConstantValueObject(maintenance.getInterval()), toolGroup);
	}

	private void createBreakdown(final FabModel model, final ToolGroup toolGroup,
			final ToolGroupSpec toolGroupSpec) {
		final BreakdownSpec breakdown = toolGroupSpec.getBreakdown();
		if (breakdown == null) {
			return;
		}
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup(
				toolGroupSpec.getName() + "_Breakdown", breakdown.getTimeToRepairDistribution(),
				breakdown.getTimeToFailureDistribution(), toolGroup);
	}

	protected LotSource createProductLine(final FabModel model, final ProductSpec productSpec,
			final Sink sink, final Map<String, ToolGroup> toolGroups,
			final Map<String, ToolGroupSpec> toolGroupSpecs,
			final SetupManager setupManager, final FabSpec fabSpec) {
		final Recipe recipe = createRecipe(model, productSpec, toolGroups, toolGroupSpecs, setupManager);
		addSinkStep(model, sink, recipe);
		final Product product = createProduct(model, productSpec, recipe);
		final LotSource source = createSource(model, productSpec, product);
		source.setLotSize(fabSpec.getLotSize());
		source.setAllowSplit(fabSpec.isAllowSplit());
		return source;
	}

	protected Recipe createRecipe(final FabModel model, final ProductSpec productSpec,
			final Map<String, ToolGroup> toolGroups, final Map<String, ToolGroupSpec> toolGroupSpecs,
			final SetupManager setupManager) {
		final Recipe recipe = model.getSimComponentFactory().createRecipe(getRecipeName(productSpec));
		final List<RouteStepSpec> steps = productSpec.getRoute().getSteps();
		for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
			final RouteStepSpec stepSpec = steps.get(stepIndex);
			final ToolGroup toolGroup = getToolGroup(toolGroups, stepSpec);
			createRouteStep(model, recipe, productSpec, stepSpec, stepIndex, toolGroup,
					getToolGroupSpec(toolGroupSpecs, stepSpec), setupManager);
		}
		return recipe;
	}

	protected ProcessStep createRouteStep(final FabModel model, final Recipe recipe,
			final ProductSpec productSpec, final RouteStepSpec stepSpec, final int stepIndex,
			final ToolGroup toolGroup, final ToolGroupSpec toolGroupSpec,
			final SetupManager setupManager) {
		validateStepToolGroupCompatibility(stepSpec, toolGroup);
		final BatchDetails batchDetails = createBatchDetails(model, productSpec, stepSpec, stepIndex, toolGroup);
		return model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				getRouteStepName(productSpec, stepSpec, stepIndex), toolGroup, null, toolGroupSpec.getLoadTime(),
				stepSpec.getProcessingTimeDistribution(), toolGroupSpec.getUnloadTime(), batchDetails,
				setupManager.getTargetSetupState(stepSpec), stepSpec.getProcessingUnit(), recipe);
	}

	protected BatchDetails createBatchDetails(final FabModel model, final ProductSpec productSpec,
			final RouteStepSpec stepSpec, final int stepIndex, final ToolGroup toolGroup) {
		if (stepSpec.getProcessingUnit() != ProcessStep.ProcessType.BATCH) {
			return null;
		}
		return model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				getBatchDetailsName(productSpec, stepSpec, stepIndex), stepSpec.getBatchMinimum(),
				stepSpec.getBatchMaximum(), toolGroup);
	}

	protected Product createProduct(final FabModel model, final ProductSpec productSpec,
			final Recipe recipe) {
		return model.getSimComponentFactory().createProduct(productSpec.getName(), recipe);
	}

	protected LotSource createSource(final FabModel model, final ProductSpec productSpec,
			final Product product) {
		final LotSource source = (LotSource) model.getSimComponentFactory().createSource(
				getSourceName(productSpec), product, productSpec.getReleaseDistribution());
		source.setDefaultPriority(productSpec.getPriority());
		source.setDueDateLeadTime(productSpec.getDueDateLeadTime());
		return source;
	}

	protected String getRecipeName(final ProductSpec productSpec) {
		return productSpec.getName() + "Recipe";
	}

	protected String getSourceName(final ProductSpec productSpec) {
		return "Source_" + productSpec.getName();
	}

	protected String getRouteStepName(final ProductSpec productSpec,
			final RouteStepSpec stepSpec, final int stepIndex) {
		return productSpec.getName() + "_Step" + (stepIndex + 1);
	}

	protected final String getBatchDetailsName(final ProductSpec productSpec,
			final RouteStepSpec stepSpec, final int stepIndex) {
		return productSpec.getName() + "_Step" + (stepIndex + 1) + "_Batch";
	}

	protected void addSinkStep(final FabModel model, final Sink sink, final Recipe recipe) {
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				sink.getName(), sink, 0L, ProcessStep.ProcessType.LOT, recipe);
	}

	private ToolGroup getToolGroup(final Map<String, ToolGroup> toolGroups, final RouteStepSpec stepSpec) {
		final ToolGroup toolGroup = toolGroups.get(stepSpec.getToolGroupName());
		if (toolGroup != null) {
			return toolGroup;
		}
		throw new IllegalArgumentException("Unknown toolgroup in route step: " + stepSpec.getToolGroupName());
	}

	private ToolGroupSpec getToolGroupSpec(final Map<String, ToolGroupSpec> toolGroupSpecs,
			final RouteStepSpec stepSpec) {
		final ToolGroupSpec toolGroupSpec = toolGroupSpecs.get(stepSpec.getToolGroupName());
		if (toolGroupSpec != null) {
			return toolGroupSpec;
		}
		throw new IllegalArgumentException("Unknown toolgroup spec in route step: " + stepSpec.getToolGroupName());
	}

	private void validateStepToolGroupCompatibility(final RouteStepSpec stepSpec, final ToolGroup toolGroup) {
		if (stepSpec.getProcessingUnit() == ProcessStep.ProcessType.BATCH
				&& toolGroup.getProcessingType() != ProcessingType.BATCH) {
			throw new IllegalArgumentException("BATCH step " + stepSpec.getToolGroupName()
					+ " must use a BATCH toolgroup");
		}
		if (stepSpec.getProcessingUnit() != ProcessStep.ProcessType.BATCH
				&& toolGroup.getProcessingType() == ProcessingType.BATCH) {
			throw new IllegalArgumentException("Non-BATCH step " + stepSpec.getToolGroupName()
					+ " must not use a BATCH toolgroup");
		}
	}

	protected abstract String getBenchmarkName();

	protected abstract double getFlowFactor();

	protected abstract FabSpec createFabSpec(FabModel model);
}
