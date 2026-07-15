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
		private long warmupTimeHours = 0L;
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
					createLocalDispatchRule(config.dispatchRuleName), config.simulationTimeHours, config.runs,
					config.warmupTimeHours, logWriter);
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
			if ("--warmup-time".equals(arg)) {
				config.warmupTimeHours = parseRequiredLong(arg, nextValue(args, ++i, arg));
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
		if (config.warmupTimeHours < 0L) {
			throw new IllegalArgumentException("--warmup-time must not be negative");
		}
		if (config.warmupTimeHours >= config.simulationTimeHours) {
			throw new IllegalArgumentException("--warmup-time must be less than --simulation-time");
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
			System.out.println("Completed wafers per day: " + formatDecimal(result.getCompletedWafersPerDay()));
			System.out.println("Tardiness per wafer: " + formatMinutes(result.getTardinessPerWaferMinutes()));
			System.out.println("Completed wafers: " + result.getCompletedWafers());
			System.out.println("Tardy wafers: " + result.getTardyWafers());
			System.out.println("Flow factor: " + formatDecimal(result.getFlowFactor()));
			return;
		}

		System.out.println("Completed wafers per day: mean="
				+ formatDecimal(result.getCompletedWafersPerDayMean()) + ", std="
				+ formatDecimal(result.getCompletedWafersPerDayStdDev()));
		System.out.println("Tardiness per wafer: mean="
				+ formatMinutes(result.getTardinessPerWaferMinutesMean()) + ", std="
				+ formatMinutes(result.getTardinessPerWaferMinutesStdDev()));
		System.out.println("Completed wafers: mean=" + formatDecimal(result.getCompletedWafersMean()) + ", std="
				+ formatDecimal(result.getCompletedWafersStdDev()));
		System.out.println("Tardy wafers: mean=" + formatDecimal(result.getTardyWafersMean()) + ", std="
				+ formatDecimal(result.getTardyWafersStdDev()));
		System.out.println("Flow factor: mean=" + formatDecimal(result.getFlowFactorMean()) + ", std="
				+ formatDecimal(result.getFlowFactorStdDev()));
	}

	private static String formatDecimal(final double value) {
		return String.format(Locale.ROOT, "%.3f", value);
	}

	private static String formatMinutes(final double durationMinutes) {
		return String.format(Locale.ROOT, "%.3f min", durationMinutes);
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
				"  java -jar fabsim.jar --simulation-time <hours> [--warmup-time <hours>] [--runs <n>] --dispatch-rule fifo|edd|srpt|random [--log-file <path>]");
	}
}
