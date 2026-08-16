package de.terministic.fabsim.benchmarks.core.results;

import java.util.List;

public final class RunResultAggregator {

	private RunResultAggregator() {
	}

	public static RunResult aggregate(final int runs, final long simulationTimeHours,
			final List<RunResult> runResults) {
		final SummaryStatistics completedWafersPerDayStatistics = summarize(runResults,
				Metric.COMPLETED_WAFERS_PER_DAY);
		final SummaryStatistics tardinessStatistics = summarize(runResults, Metric.TARDINESS_PER_WAFER_MINUTES);
		final SummaryStatistics completedWafersStatistics = summarize(runResults, Metric.COMPLETED_WAFERS);
		final SummaryStatistics tardyWafersStatistics = summarize(runResults, Metric.TARDY_WAFERS);
		final SummaryStatistics flowFactorStatistics = summarize(runResults, Metric.FLOW_FACTOR);
		return new RunResult(runs, simulationTimeHours,
				completedWafersPerDayStatistics.mean, completedWafersPerDayStatistics.stdDev,
				tardinessStatistics.mean, tardinessStatistics.stdDev,
				completedWafersStatistics.mean, completedWafersStatistics.stdDev, tardyWafersStatistics.mean,
				tardyWafersStatistics.stdDev, flowFactorStatistics.mean, flowFactorStatistics.stdDev);
	}

	private static SummaryStatistics summarize(final List<RunResult> runResults, final Metric metric) {
		final double[] values = new double[runResults.size()];
		for (int i = 0; i < runResults.size(); i++) {
			values[i] = metric.valueOf(runResults.get(i));
		}
		return SummaryStatistics.of(values);
	}

	private enum Metric {
		COMPLETED_WAFERS_PER_DAY {
			@Override
			double valueOf(final RunResult result) {
				return result.getCompletedWafersPerDay();
			}
		},
		TARDINESS_PER_WAFER_MINUTES {
			@Override
			double valueOf(final RunResult result) {
				return result.getTardinessPerWaferMinutes();
			}
		},
		COMPLETED_WAFERS {
			@Override
			double valueOf(final RunResult result) {
				return result.getCompletedWafers();
			}
		},
		TARDY_WAFERS {
			@Override
			double valueOf(final RunResult result) {
				return result.getTardyWafers();
			}
		},
		FLOW_FACTOR {
			@Override
			double valueOf(final RunResult result) {
				return result.getFlowFactor();
			}
		};

		abstract double valueOf(RunResult result);
	}
}
