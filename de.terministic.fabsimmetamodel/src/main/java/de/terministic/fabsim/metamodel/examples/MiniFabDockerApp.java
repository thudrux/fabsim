package de.terministic.fabsim.metamodel.examples;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.EDD;
import de.terministic.fabsim.metamodel.dispatchRules.FIFO;
import de.terministic.fabsim.metamodel.dispatchRules.Random;
import de.terministic.fabsim.metamodel.dispatchRules.SRPT;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;

public final class MiniFabDockerApp {

	private static final class CliConfig {
		private long simulationTimeHours = -1L;
		private int runs = 1;
		private String dispatchRuleName;
		private Path logFile;
		private boolean help;
	}

	public static void main(final String[] args) {
		final MiniFabDockerApp app = new MiniFabDockerApp();
		try {
			final int exitCode = app.run(args);
			System.exit(exitCode);
		} catch (final IllegalArgumentException ex) {
			System.err.println("Error: " + ex.getMessage());
			printUsage();
			System.exit(2);
		} catch (final Exception ex) {
			System.err.println("MiniFab execution failed: " + ex.getMessage());
			ex.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private int run(final String[] args) {
		final CliConfig config = parseArguments(args);
		if (config.help) {
			printUsage();
			return 0;
		}

		final MiniFab miniFab = new MiniFab();
		final LocalLogWriter logWriter = config.logFile == null ? null : new LocalLogWriter(config.logFile);
		try {
			final MiniFabRunResult result = miniFab.runMiniFabWithLocalDispatch(
					createLocalDispatchRule(config.dispatchRuleName), config.simulationTimeHours, config.runs, logWriter);
			printResult(result);
			if (config.logFile != null) {
				System.out.println("Dispatch log written to " + config.logFile + " (JSONL)");
			}
			return 0;
		} finally {
			if (logWriter != null) {
				logWriter.close();
			}
		}
	}

	private AbstractDispatchRule createLocalDispatchRule(final String dispatchRuleName) {
		if (dispatchRuleName == null || dispatchRuleName.trim().isEmpty()) {
			throw new IllegalArgumentException("--dispatch-rule fifo|edd|srpt|random is required");
		}
		final String normalizedDispatchRuleName = dispatchRuleName.trim().toLowerCase(Locale.ROOT);
		if ("fifo".equals(normalizedDispatchRuleName)) {
			return new FIFO();
		}
		if ("edd".equals(normalizedDispatchRuleName)) {
			return new EDD();
		}
		if ("srpt".equals(normalizedDispatchRuleName)) {
			return new SRPT();
		}
		if ("random".equals(normalizedDispatchRuleName)) {
			return new Random();
		}
		throw new IllegalArgumentException("Unsupported local dispatch rule: " + dispatchRuleName);
	}

	private CliConfig parseArguments(final String[] args) {
		final CliConfig config = new CliConfig();
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if ("--help".equals(arg) || "-h".equals(arg)) {
				config.help = true;
				return config;
			}
			if (!arg.startsWith("--")) {
				throw new IllegalArgumentException("Unexpected argument: " + arg);
			}
			if ("--simulation-time".equals(arg)) {
				config.simulationTimeHours = parseRequiredLong(arg, nextValue(args, ++i, arg));
				continue;
			}
			if ("--runs".equals(arg)) {
				config.runs = parseRequiredInt(arg, nextValue(args, ++i, arg));
				continue;
			}
			if ("--dispatch-rule".equals(arg)) {
				config.dispatchRuleName = nextValue(args, ++i, arg);
				continue;
			}
			if ("--log-file".equals(arg)) {
				config.logFile = parsePath(nextValue(args, ++i, arg));
				continue;
			}
			throw new IllegalArgumentException("Unknown option: " + arg);
		}

		validate(config);
		return config;
	}

	private void validate(final CliConfig config) {
		if (config.simulationTimeHours <= 0L) {
			throw new IllegalArgumentException("--simulation-time must be a positive number of hours");
		}
		if (config.dispatchRuleName == null) {
			throw new IllegalArgumentException("--dispatch-rule fifo|edd|srpt|random is required");
		}
		if (config.runs <= 0) {
			throw new IllegalArgumentException("--runs must be a positive number");
		}
		if (config.runs > 1 && config.logFile != null) {
			throw new IllegalArgumentException("--log-file is only supported when --runs is 1");
		}
	}

	private long parseRequiredLong(final String optionName, final String value) {
		try {
			return Long.parseLong(value);
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException(optionName + " requires a numeric value");
		}
	}

	private int parseRequiredInt(final String optionName, final String value) {
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException ex) {
			throw new IllegalArgumentException(optionName + " requires a numeric value");
		}
	}

	private Path parsePath(final String value) {
		try {
			return Paths.get(value);
		} catch (final InvalidPathException ex) {
			throw new IllegalArgumentException("--log-file contains an invalid path: " + value, ex);
		}
	}

	private void printResult(final MiniFabRunResult result) {
		if (result.getRuns() == 1L) {
			System.out.println("MiniFab completed at simulation time " + result.getSimulationTimeHours()
					+ " h (" + result.getSimulationTimeMillis() + " ms)");
			System.out.println("Throughput: " + result.getThroughput() + " finished wafers");
			System.out.println("Tardy: " + result.getTardyWafers() + " finished wafers");
			System.out.println("Total Weighted Tardiness: " + formatScientificMillis(result.getTotalWeightedTardiness()));
			return;
		}

		System.out.println("MiniFab completed at simulation time " + formatDecimal(result.getSimulationTimeHoursMean())
				+ " h (" + formatDecimal(result.getSimulationTimeMillisMean()) + " ms)");
		System.out.println("Throughput: mean=" + formatDecimal(result.getThroughputMean()) + ", std="
				+ formatDecimal(result.getThroughputStdDev()) + " finished wafers");
		System.out.println("Tardy: mean=" + formatDecimal(result.getTardyWafersMean()) + ", std="
				+ formatDecimal(result.getTardyWafersStdDev()) + " finished wafers");
		System.out.println("Total Weighted Tardiness: mean="
				+ formatScientificMillis(result.getTotalWeightedTardinessMean()) + ", std="
				+ formatScientificMillis(result.getTotalWeightedTardinessStdDev()));
	}

	private static String formatDecimal(final double value) {
		return String.format(Locale.ROOT, "%.3f", Double.valueOf(value));
	}

	private static String formatScientificMillis(final double durationMillis) {
		return String.format(Locale.ROOT, "%.3e ms", Double.valueOf(durationMillis));
	}

	private String nextValue(final String[] args, final int index, final String optionName) {
		if (index >= args.length) {
			throw new IllegalArgumentException(optionName + " requires a value");
		}
		final String value = args[index];
		if (value.startsWith("--")) {
			throw new IllegalArgumentException(optionName + " requires a value");
		}
		return value;
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println(
				"  java -jar minifab.jar --simulation-time <hours> [--runs <n>] --dispatch-rule fifo|edd|srpt|random [--log-file <path>]");
	}
}
