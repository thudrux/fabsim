package de.terministic.fabsim.benchmarks.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.benchmarks.core.batchrules.SameProductAndStepBatchRule;
import de.terministic.fabsim.benchmarks.core.results.RunResult;
import de.terministic.fabsim.benchmarks.core.results.RunResultAggregator;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkBreakdownSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkFabSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkMaintenanceSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkProductSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkRouteStepSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkToolGroupSpec;
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

	private FabModel model;
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
		this.model = createFabModel();
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
		this.model = createFabModel();
	}

	public final RunResult run(final long simulationTimeHours, final int runs, final long warmupTimeHours) {
		if (runs <= 0) {
			throw new IllegalArgumentException("runs must be a positive number");
		}
		if (this.model == null) {
			throw new IllegalStateException(getBenchmarkName() + " must be built before running the simulation");
		}
		if (runs > 1 && (this.logWriter != null || this.dispatchRule instanceof LoggingDispatchRule)) {
			throw new IllegalArgumentException("Logging is only supported for a single " + getBenchmarkName() + " run");
		}
		if (simulationTimeHours <= 0L) {
			throw new IllegalArgumentException("simulationTimeHours must be a positive number");
		}
		if (warmupTimeHours < 0L) {
			throw new IllegalArgumentException("warmupTimeHours must not be negative");
		}
		if (warmupTimeHours >= simulationTimeHours) {
			throw new IllegalArgumentException("warmupTimeHours must be less than simulationTimeHours");
		}

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

	private FabModel createFabModel() {
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
		final FabModel fabModel = new FabModel();
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
			final BenchmarkFabSpec fabSpec) {
		final Sink sink = (Sink) model.getSimComponentFactory().createSink(fabSpec.getSinkName());
		final Map<String, ToolGroup> toolGroups = createToolGroups(model, dispatchRule, fabSpec);
		final Map<String, BenchmarkToolGroupSpec> toolGroupSpecs = getToolGroupSpecs(fabSpec);
		for (final BenchmarkProductSpec productSpec : fabSpec.getProducts()) {
			createProductLine(model, productSpec, sink, toolGroups, toolGroupSpecs, fabSpec);
		}
		return model;
	}

	private Map<String, BenchmarkToolGroupSpec> getToolGroupSpecs(final BenchmarkFabSpec fabSpec) {
		final LinkedHashMap<String, BenchmarkToolGroupSpec> toolGroupSpecs = new LinkedHashMap<>();
		for (final BenchmarkToolGroupSpec toolGroupSpec : fabSpec.getToolGroups()) {
			toolGroupSpecs.put(toolGroupSpec.getName(), toolGroupSpec);
		}
		return toolGroupSpecs;
	}

	protected Map<String, ToolGroup> createToolGroups(final FabModel model, final AbstractDispatchRule dispatchRule,
			final BenchmarkFabSpec fabSpec) {
		final LinkedHashMap<String, ToolGroup> toolGroups = new LinkedHashMap<>();
		for (final BenchmarkToolGroupSpec toolGroupSpec : fabSpec.getToolGroups()) {
			if (toolGroups.containsKey(toolGroupSpec.getName())) {
				throw new IllegalArgumentException("Duplicate toolgroup name: " + toolGroupSpec.getName());
			}
			final ToolGroup toolGroup = createToolGroup(model, dispatchRule, toolGroupSpec);
			toolGroups.put(toolGroupSpec.getName(), toolGroup);
		}
		return toolGroups;
	}

	protected ToolGroup createToolGroup(final FabModel model, final AbstractDispatchRule dispatchRule,
			final BenchmarkToolGroupSpec toolGroupSpec) {
		final ToolGroup toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup(
				toolGroupSpec.getName(), toolGroupSpec.getNumberOfTools(), toolGroupSpec.getProcessingType());
		toolGroup.setDispatchRule(dispatchRule);
		if (toolGroupSpec.getProcessingType() == ProcessingType.BATCH) {
			toolGroup.setBatchRule(new SameProductAndStepBatchRule(model));
		}
		createPredictiveMaintenance(model, toolGroup, toolGroupSpec);
		createBreakdown(model, toolGroup, toolGroupSpec);
		return toolGroup;
	}

	private void createPredictiveMaintenance(final FabModel model, final ToolGroup toolGroup,
			final BenchmarkToolGroupSpec toolGroupSpec) {
		final BenchmarkMaintenanceSpec maintenance = toolGroupSpec.getMaintenance();
		model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup(
				toolGroupSpec.getName() + "_PredictiveMaintenance", maintenance.getDurationDistribution(),
				model.getValueObjectFactory().createConstantValueObject(maintenance.getInterval()), toolGroup);
	}

	private void createBreakdown(final FabModel model, final ToolGroup toolGroup,
			final BenchmarkToolGroupSpec toolGroupSpec) {
		final BenchmarkBreakdownSpec breakdown = toolGroupSpec.getBreakdown();
		if (breakdown == null) {
			return;
		}
		model.getSimComponentFactory().createSimulationTimeBasedBreakdownAndAddToToolGroup(
				toolGroupSpec.getName() + "_Breakdown", breakdown.getTimeToRepairDistribution(),
				breakdown.getTimeToFailureDistribution(), toolGroup);
	}

	protected LotSource createProductLine(final FabModel model, final BenchmarkProductSpec productSpec,
			final Sink sink, final Map<String, ToolGroup> toolGroups,
			final Map<String, BenchmarkToolGroupSpec> toolGroupSpecs, final BenchmarkFabSpec fabSpec) {
		final Recipe recipe = createRecipe(model, productSpec, toolGroups, toolGroupSpecs);
		addSinkStep(model, sink, recipe);
		final Product product = createProduct(model, productSpec, recipe);
		final LotSource source = createSource(model, productSpec, product);
		source.setLotSize(fabSpec.getLotSize());
		source.setAllowSplit(fabSpec.isAllowSplit());
		return source;
	}

	protected Recipe createRecipe(final FabModel model, final BenchmarkProductSpec productSpec,
			final Map<String, ToolGroup> toolGroups, final Map<String, BenchmarkToolGroupSpec> toolGroupSpecs) {
		final Recipe recipe = model.getSimComponentFactory().createRecipe(getRecipeName(productSpec));
		final List<BenchmarkRouteStepSpec> steps = productSpec.getRoute().getSteps();
		for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
			final BenchmarkRouteStepSpec stepSpec = steps.get(stepIndex);
			final ToolGroup toolGroup = getToolGroup(toolGroups, stepSpec);
			createRouteStep(model, recipe, productSpec, stepSpec, stepIndex, toolGroup,
					getToolGroupSpec(toolGroupSpecs, stepSpec));
		}
		return recipe;
	}

	protected ProcessStep createRouteStep(final FabModel model, final Recipe recipe,
			final BenchmarkProductSpec productSpec, final BenchmarkRouteStepSpec stepSpec, final int stepIndex,
			final ToolGroup toolGroup, final BenchmarkToolGroupSpec toolGroupSpec) {
		validateStepToolGroupCompatibility(stepSpec, toolGroup);
		final BatchDetails batchDetails = createBatchDetails(model, productSpec, stepSpec, stepIndex, toolGroup);
		return model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				getRouteStepName(productSpec, stepSpec, stepIndex), toolGroup, null, toolGroupSpec.getLoadTime(),
				stepSpec.getProcessingTimeDistribution(), toolGroupSpec.getUnloadTime(), batchDetails, null,
				stepSpec.getProcessingUnit(), recipe);
	}

	protected BatchDetails createBatchDetails(final FabModel model, final BenchmarkProductSpec productSpec,
			final BenchmarkRouteStepSpec stepSpec, final int stepIndex, final ToolGroup toolGroup) {
		if (stepSpec.getProcessingUnit() != ProcessStep.ProcessType.BATCH) {
			return null;
		}
		return model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup(
				getBatchDetailsName(productSpec, stepSpec, stepIndex), stepSpec.getBatchMinimum(),
				stepSpec.getBatchMaximum(), toolGroup);
	}

	protected Product createProduct(final FabModel model, final BenchmarkProductSpec productSpec,
			final Recipe recipe) {
		return model.getSimComponentFactory().createProduct(productSpec.getName(), recipe);
	}

	protected LotSource createSource(final FabModel model, final BenchmarkProductSpec productSpec,
			final Product product) {
		final LotSource source = (LotSource) model.getSimComponentFactory().createSource(
				getSourceName(productSpec), product, productSpec.getReleaseDistribution());
		source.setDefaultPriority(productSpec.getPriority());
		source.setDueDateLeadTime(productSpec.getDueDateLeadTime());
		return source;
	}

	protected String getRecipeName(final BenchmarkProductSpec productSpec) {
		return productSpec.getName() + "Recipe";
	}

	protected String getSourceName(final BenchmarkProductSpec productSpec) {
		return "Source_" + productSpec.getName();
	}

	protected String getRouteStepName(final BenchmarkProductSpec productSpec,
			final BenchmarkRouteStepSpec stepSpec, final int stepIndex) {
		return productSpec.getName() + "_Step" + (stepIndex + 1);
	}

	protected String getBatchDetailsName(final BenchmarkProductSpec productSpec,
			final BenchmarkRouteStepSpec stepSpec, final int stepIndex) {
		return productSpec.getName() + "_Step" + (stepIndex + 1) + "_Batch";
	}

	protected void addSinkStep(final FabModel model, final Sink sink, final Recipe recipe) {
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				sink.getName(), sink, 0L, ProcessStep.ProcessType.LOT, recipe);
	}

	private ToolGroup getToolGroup(final Map<String, ToolGroup> toolGroups, final BenchmarkRouteStepSpec stepSpec) {
		final ToolGroup toolGroup = toolGroups.get(stepSpec.getToolGroupName());
		if (toolGroup != null) {
			return toolGroup;
		}
		throw new IllegalArgumentException("Unknown toolgroup in route step: " + stepSpec.getToolGroupName());
	}

	private BenchmarkToolGroupSpec getToolGroupSpec(final Map<String, BenchmarkToolGroupSpec> toolGroupSpecs,
			final BenchmarkRouteStepSpec stepSpec) {
		final BenchmarkToolGroupSpec toolGroupSpec = toolGroupSpecs.get(stepSpec.getToolGroupName());
		if (toolGroupSpec != null) {
			return toolGroupSpec;
		}
		throw new IllegalArgumentException("Unknown toolgroup spec in route step: " + stepSpec.getToolGroupName());
	}

	private void validateStepToolGroupCompatibility(final BenchmarkRouteStepSpec stepSpec, final ToolGroup toolGroup) {
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

	protected abstract BenchmarkFabSpec createFabSpec(FabModel model);
}
