package de.terministic.fabsim.metamodel.benchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.metamodel.benchmarks.results.RunResult;
import de.terministic.fabsim.metamodel.benchmarks.results.RunResultAggregator;
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
		return assembleFabModel(new FabModel(), effectiveRule);
	}

	private RunResult runSimulation(final FabModel model, final long simulationTimeHours,
			final long warmupTimeHours) {
		final SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		final long simulationTimeMillis = Math.multiplyExact(simulationTimeHours, HOUR);
		final long warmupTimeMillis = Math.multiplyExact(warmupTimeHours, HOUR);
		final long measurementTimeHours = Math.subtractExact(simulationTimeHours, warmupTimeHours);
		final FinishedLotStatisticsCollector finishedLotStatisticsCollector = new FinishedLotStatisticsCollector(model,
				getPriorityWeights(), warmupTimeMillis);
		engine.addListener(finishedLotStatisticsCollector);
		engine.runSimulation(simulationTimeMillis);
		return new RunResult(simulationTimeHours, measurementTimeHours,
				finishedLotStatisticsCollector.getFinishedWafers(),
				finishedLotStatisticsCollector.getTardyWafers(),
				finishedLotStatisticsCollector.getTardinessPerWaferMinutes(),
				finishedLotStatisticsCollector.getFlowFactorMean());
	}

	protected abstract String getBenchmarkName();

	protected abstract double getFlowFactor();

	protected abstract Map<Integer, Integer> getPriorityWeights();

	protected abstract FabModel assembleFabModel(FabModel model, AbstractDispatchRule dispatchRule);
}
