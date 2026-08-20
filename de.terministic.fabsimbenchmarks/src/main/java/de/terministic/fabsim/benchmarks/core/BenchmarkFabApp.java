package de.terministic.fabsim.benchmarks.core;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.terministic.fabsim.benchmarks.implementations.MiniFab;
import de.terministic.fabsim.benchmarks.core.results.RunResult;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.CriticalRatio;
import de.terministic.fabsim.metamodel.dispatchRules.EDD;
import de.terministic.fabsim.metamodel.dispatchRules.FIFO;
import de.terministic.fabsim.metamodel.dispatchRules.Random;
import de.terministic.fabsim.metamodel.dispatchRules.SRPT;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;

public final class BenchmarkFabApp {

	private static final List<FabImplementation> FAB_IMPLEMENTATIONS = Arrays.asList(
			new FabImplementation("minifab", MiniFab::new));

	private static final class CliConfig {
		private String fabName;
		private long simulationTimeHours = -1L;
		private long warmupTimeHours = 0L;
		private String dispatchRuleName;
		private Path logFile;
		private boolean help;
	}

	private interface FabFactory {
		BenchmarkFab create(AbstractDispatchRule dispatchRule, LocalLogWriter logWriter);
	}

	private static final class FabImplementation {
		private final String name;
		private final FabFactory factory;

		private FabImplementation(final String name, final FabFactory factory) {
			this.name = name;
			this.factory = factory;
		}

		private String getName() {
			return this.name;
		}

		private BenchmarkFab create(final AbstractDispatchRule dispatchRule, final LocalLogWriter logWriter) {
			return this.factory.create(dispatchRule, logWriter);
		}
	}

	public static void main(final String[] args) {
		final BenchmarkFabApp app = new BenchmarkFabApp();
		try {
			final int exitCode = app.run(args);
			System.exit(exitCode);
		} catch (final IllegalArgumentException ex) {
			System.err.println("Error: " + ex.getMessage());
			printUsage();
			System.exit(2);
		} catch (final Exception ex) {
			System.err.println("Fab execution failed: " + ex.getMessage());
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

		final LocalLogWriter logWriter = config.logFile == null ? null : new LocalLogWriter(config.logFile);
		try {
			runFab(config, logWriter);
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

	private void runFab(final CliConfig config, final LocalLogWriter logWriter) {
		final FabImplementation fabImplementation = findFabImplementation(config.fabName);
		final BenchmarkFab fab = fabImplementation.create(createLocalDispatchRule(config.dispatchRuleName), logWriter);
		final RunResult result = fab.run(config.simulationTimeHours, config.warmupTimeHours);
		printFabResult(result);
	}

	private FabImplementation findFabImplementation(final String fabName) {
		for (final FabImplementation fabImplementation : FAB_IMPLEMENTATIONS) {
			if (fabImplementation.getName().equals(fabName)) {
				return fabImplementation;
			}
		}
		throw new IllegalArgumentException("Unsupported fab implementation: " + fabName);
	}

	private AbstractDispatchRule createLocalDispatchRule(final String dispatchRuleName) {
		if (dispatchRuleName == null || dispatchRuleName.trim().isEmpty()) {
			throw new IllegalArgumentException("--dispatch-rule random|fifo|edd|cr|srpt is required");
		}
		final String normalizedDispatchRuleName = dispatchRuleName.trim().toLowerCase(Locale.ROOT);
		if ("random".equals(normalizedDispatchRuleName)) {
			return new Random();
		}
		if ("fifo".equals(normalizedDispatchRuleName)) {
			return new FIFO();
		}
		if ("edd".equals(normalizedDispatchRuleName)) {
			return new EDD();
		}
		if ("cr".equals(normalizedDispatchRuleName)) {
			return new CriticalRatio();
		}
		if ("srpt".equals(normalizedDispatchRuleName)) {
			return new SRPT();
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
			if ("--fab".equals(arg)) {
				config.fabName = normalizeFabName(nextValue(args, ++i, arg));
				continue;
			}
			if ("--simulation-time".equals(arg)) {
				config.simulationTimeHours = parseRequiredLong(arg, nextValue(args, ++i, arg));
				continue;
			}
			if ("--warmup-time".equals(arg)) {
				config.warmupTimeHours = parseRequiredLong(arg, nextValue(args, ++i, arg));
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
		if (config.fabName == null) {
			throw new IllegalArgumentException("--fab " + supportedFabNames() + " is required");
		}
		if (!isSupportedFab(config.fabName)) {
			throw new IllegalArgumentException("--fab must be one of: " + supportedFabNames());
		}
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
			throw new IllegalArgumentException("--dispatch-rule random|fifo|edd|cr|srpt is required");
		}
	}

	private String normalizeFabName(final String fabName) {
		if (fabName == null || fabName.trim().isEmpty()) {
			throw new IllegalArgumentException("--fab requires a value");
		}
		return fabName.trim().toLowerCase(Locale.ROOT);
	}

	private long parseRequiredLong(final String optionName, final String value) {
		try {
			return Long.parseLong(value);
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

	private boolean isSupportedFab(final String fabName) {
		for (final FabImplementation fabImplementation : FAB_IMPLEMENTATIONS) {
			if (fabImplementation.getName().equals(fabName)) {
				return true;
			}
		}
		return false;
	}

	private String supportedFabNames() {
		final StringBuilder names = new StringBuilder();
		for (final FabImplementation fabImplementation : FAB_IMPLEMENTATIONS) {
			if (names.length() > 0) {
				names.append("|");
			}
			names.append(fabImplementation.getName());
		}
		return names.toString();
	}

	private static void printFabResult(final RunResult result) {
		System.out.println("Completed wafers per day: " + formatDecimal(result.getCompletedWafersPerDay()));
		System.out.println("Tardiness per wafer: " + formatMinutes(result.getTardinessPerWaferMinutes()));
		System.out.println("Completed wafers: " + result.getCompletedWafers());
		System.out.println("Tardy wafers: " + result.getTardyWafers());
		System.out.println("Flow factor: " + formatDecimal(result.getFlowFactor()));
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
		final String supportedFabNames = new BenchmarkFabApp().supportedFabNames();
		System.out.println("Usage:");
		System.out.println(
				"  java -jar <benchmarks-jar> --fab " + supportedFabNames
						+ " --simulation-time <hours> [--warmup-time <hours>] --dispatch-rule random|fifo|edd|cr|srpt [--log-file <path>]");
	}
}
