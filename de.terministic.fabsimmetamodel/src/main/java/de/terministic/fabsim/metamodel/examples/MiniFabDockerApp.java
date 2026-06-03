package de.terministic.fabsim.metamodel.examples;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.FIFO;
import de.terministic.fabsim.metamodel.externaldispatch.ExternalDispatchConfiguration;

public final class MiniFabDockerApp {

	private static final long HOUR_IN_MILLISECONDS = 60L * 60L * 1000L;
	private static final long DEFAULT_EXTERNAL_DISPATCH_TIMEOUT_MS =
			ExternalDispatchConfiguration.localDefault().getTimeoutMillis();
	private static final int PROGRESS_BAR_WIDTH = 30;

	private enum Mode {
		EXTERNAL,
		LOCAL
	}

	private static final class CliConfig {
		private long simulationTimeHours = -1L;
		private Mode mode;
		private String dispatchRuleName;
		private String dispatchHost;
		private Integer dispatchPort;
		private Long dispatchTimeoutMillis;
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
		final FabModel model = buildModel(miniFab, config);
		final SimulationEngine engine = new FabSimulationEngine();
		engine.init(model);
		final long simulationTimeMillis = toSimulationTimeMillis(config.simulationTimeHours);
		final SimulationProgressListener progressListener = new SimulationProgressListener(simulationTimeMillis);
		engine.addListener(progressListener);
		try {
			progressListener.printProgress(0L);
			engine.runSimulation(simulationTimeMillis);
		} finally {
			progressListener.finish();
		}

		System.out.println("MiniFab completed at simulation time " + config.simulationTimeHours
				+ " h (" + simulationTimeMillis + " ms)");
		if (config.logFile != null) {
			System.out.println("Dispatch log written to " + config.logFile);
		}
		return 0;
	}

	private FabModel buildModel(final MiniFab miniFab, final CliConfig config) {
		if (config.mode == Mode.EXTERNAL) {
			final ExternalDispatchConfiguration dispatchConfiguration = new ExternalDispatchConfiguration(
					config.dispatchHost, config.dispatchPort.intValue(),
					config.dispatchTimeoutMillis == null ? DEFAULT_EXTERNAL_DISPATCH_TIMEOUT_MS
							: config.dispatchTimeoutMillis.longValue());
			return miniFab.createMiniFabModelWithExternalDispatch(dispatchConfiguration, config.logFile);
		}
		if (config.mode == Mode.LOCAL) {
			final AbstractDispatchRule dispatchRule = createLocalDispatchRule(config.dispatchRuleName);
			return miniFab.createMiniFabModelWithDispatchRule(dispatchRule, config.logFile);
		}
		throw new IllegalArgumentException("mode must be set");
	}

	private AbstractDispatchRule createLocalDispatchRule(final String dispatchRuleName) {
		if (dispatchRuleName == null || dispatchRuleName.trim().isEmpty()) {
			throw new IllegalArgumentException("local mode requires --dispatch-rule fifo");
		}
		if ("fifo".equals(dispatchRuleName.trim().toLowerCase(Locale.ROOT))) {
			return new FIFO();
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
			if ("--mode".equals(arg)) {
				final String modeValue = nextValue(args, ++i, arg);
				config.mode = parseMode(modeValue);
				continue;
			}
			if ("--dispatch-rule".equals(arg)) {
				config.dispatchRuleName = nextValue(args, ++i, arg);
				continue;
			}
			if ("--dispatch-host".equals(arg)) {
				config.dispatchHost = nextValue(args, ++i, arg);
				continue;
			}
			if ("--dispatch-port".equals(arg)) {
				config.dispatchPort = Integer.valueOf(parseRequiredInt(arg, nextValue(args, ++i, arg)));
				continue;
			}
			if ("--dispatch-timeout-ms".equals(arg)) {
				config.dispatchTimeoutMillis = Long.valueOf(parseRequiredLong(arg, nextValue(args, ++i, arg)));
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
		if (config.mode == null) {
			throw new IllegalArgumentException("--mode must be provided");
		}
		switch (config.mode) {
		case EXTERNAL:
			if (config.dispatchRuleName != null) {
				throw new IllegalArgumentException("--dispatch-rule is only valid for local mode");
			}
			if (config.dispatchHost == null || config.dispatchHost.trim().isEmpty()) {
				throw new IllegalArgumentException("external mode requires --dispatch-host");
			}
			if (config.dispatchPort == null) {
				throw new IllegalArgumentException("external mode requires --dispatch-port");
			}
			if (config.dispatchPort.intValue() <= 0) {
				throw new IllegalArgumentException("--dispatch-port must be positive");
			}
			break;
		case LOCAL:
			if (config.dispatchHost != null || config.dispatchPort != null || config.dispatchTimeoutMillis != null) {
				throw new IllegalArgumentException("host, port, and timeout are only valid for external mode");
			}
			if (config.dispatchRuleName == null) {
				throw new IllegalArgumentException("local mode requires --dispatch-rule fifo");
			}
			break;
		default:
			throw new IllegalArgumentException("Unsupported mode: " + config.mode);
		}
	}

	private Mode parseMode(final String modeValue) {
		if (modeValue == null) {
			throw new IllegalArgumentException("--mode requires a value");
		}
		final String normalized = modeValue.trim().toLowerCase(Locale.ROOT);
		if ("external".equals(normalized)) {
			return Mode.EXTERNAL;
		}
		if ("local".equals(normalized)) {
			return Mode.LOCAL;
		}
		throw new IllegalArgumentException("Unsupported mode: " + modeValue);
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

	private long toSimulationTimeMillis(final long simulationTimeHours) {
		try {
			return Math.multiplyExact(simulationTimeHours, HOUR_IN_MILLISECONDS);
		} catch (final ArithmeticException ex) {
			throw new IllegalArgumentException("--simulation-time is too large to convert to milliseconds", ex);
		}
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
				"  java -jar minifab.jar --mode external --simulation-time <hours> --dispatch-host <host> --dispatch-port <port> [--dispatch-timeout-ms <ms>] [--log-file <path>]");
		System.out.println(
				"  java -jar minifab.jar --mode local --simulation-time <hours> --dispatch-rule fifo [--log-file <path>]");
	}

	private static final class SimulationProgressListener extends SimEventListener {

		private final long endTimeMillis;
		private int lastPrintedPercent = -1;

		private SimulationProgressListener(final long endTimeMillis) {
			this.endTimeMillis = endTimeMillis;
		}

		@Override
		public void processEvent(final ISimEvent event) {
			printProgress(event.getEventTime());
		}

		private void printProgress(final long simulationTimeMillis) {
			if (this.endTimeMillis <= 0L) {
				return;
			}
			final int percent = (int) Math.min(100L, (simulationTimeMillis * 100L) / this.endTimeMillis);
			if (percent <= this.lastPrintedPercent) {
				return;
			}
			this.lastPrintedPercent = percent;
			final String bar = buildBar(percent);
			System.err.print("\r" + bar + " " + percent + "%");
			System.err.flush();
		}

		private String buildBar(final int percent) {
			final int filled = Math.min(PROGRESS_BAR_WIDTH, (percent * PROGRESS_BAR_WIDTH) / 100);
			final StringBuilder bar = new StringBuilder(PROGRESS_BAR_WIDTH + 2);
			bar.append('[');
			for (int i = 0; i < PROGRESS_BAR_WIDTH; i++) {
				bar.append(i < filled ? '#' : '-');
			}
			bar.append(']');
			return bar.toString();
		}

		private void finish() {
			printProgress(this.endTimeMillis);
			System.err.println();
			System.err.flush();
		}
	}
}
