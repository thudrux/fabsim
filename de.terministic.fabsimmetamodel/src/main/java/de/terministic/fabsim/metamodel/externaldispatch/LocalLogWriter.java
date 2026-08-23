package de.terministic.fabsim.metamodel.externaldispatch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.CostSnapshot;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.FabStateSnapshot;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.FlowItemInProcessSnapshot;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.FlowItemQueuedSnapshot;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.ToolGroupSnapshot;
import de.terministic.fabsim.metamodel.externaldispatch.snapshots.ToolSnapshot;

public final class LocalLogWriter implements AutoCloseable {

	private final Path logFile;
	private final BufferedWriter writer;
	private boolean closed;

	public LocalLogWriter(final Path logFile) {
		this.logFile = logFile;
		if (logFile == null) {
			this.writer = null;
			return;
		}
		try {
			final Path parent = logFile.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			this.writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (final IOException ex) {
			throw new IllegalStateException("Failed to open dispatch decision log at " + logFile, ex);
		}
	}

	public synchronized void append(final DispatchDecisionRequest request, final AbstractFlowItem selectedFlowItem) {
		if (request == null || selectedFlowItem == null || this.writer == null) {
			return;
		}
		if (this.closed) {
			throw new IllegalStateException("Dispatch decision log writer has already been closed for " + this.logFile);
		}
		final FlowItemQueuedSnapshot chosenFlowItem = FlowItemQueuedSnapshot.capture(selectedFlowItem,
				request.getSimulationTime(), request.getProjectedCycleTimeFactor());
		try {
			this.writer.write(toJson(request.getFabState(), chosenFlowItem));
			this.writer.write('\n');
		} catch (final IOException ex) {
			throw new IllegalStateException("Failed to write dispatch decision log to " + this.logFile, ex);
		}
	}

	@Override
	public synchronized void close() {
		if (this.writer == null || this.closed) {
			return;
		}
		this.closed = true;
		try {
			this.writer.flush();
			this.writer.close();
		} catch (final IOException ex) {
			throw new IllegalStateException("Failed to close dispatch decision log at " + this.logFile, ex);
		}
	}

	private String toJson(final FabStateSnapshot fabState, final FlowItemQueuedSnapshot chosenFlowItem) {
		final StringBuilder builder = new StringBuilder();
		builder.append('{');
		builder.append("\"fab_state\":");
		appendFabState(builder, fabState);
		builder.append(',');
		builder.append("\"dispatch_decision\":");
		appendDispatchDecision(builder, chosenFlowItem);
		builder.append('}');
		return builder.toString();
	}

	private void appendDispatchDecision(final StringBuilder builder, final FlowItemQueuedSnapshot chosenFlowItem) {
		builder.append('{');
		builder.append("\"chosen_flow_item\":");
		appendFlowItemQueued(builder, chosenFlowItem);
		builder.append('}');
	}

	private void appendFabState(final StringBuilder builder, final FabStateSnapshot fabState) {
		builder.append('{');
		builder.append("\"simulation_time\":").append(fabState.getSimulationTime()).append(',');
		builder.append("\"tool_groups\":");
		builder.append('[');
		final List<ToolGroupSnapshot> toolGroups = fabState.getToolGroups();
		for (int i = 0; i < toolGroups.size(); i++) {
			if (i > 0) {
				builder.append(',');
			}
			appendToolGroup(builder, toolGroups.get(i));
		}
		builder.append(']');
		builder.append(',');
		builder.append("\"cost_snapshot\":");
		appendCostSnapshot(builder, fabState.getCostSnapshot());
		builder.append('}');
	}

	private void appendCostSnapshot(final StringBuilder builder, final CostSnapshot costSnapshot) {
		builder.append('{');
		builder.append("\"total_projected_tardiness\":")
				.append(costSnapshot.getTotalProjectedTardiness()).append(',');
		builder.append("\"work_in_progress\":").append(costSnapshot.getWorkInProgress());
		builder.append('}');
	}

	private void appendToolGroup(final StringBuilder builder, final ToolGroupSnapshot toolGroup) {
		builder.append('{');
		builder.append("\"name\":").append(quote(toolGroup.getName())).append(',');
		builder.append("\"waiting_for_dispatch\":").append(toolGroup.getWaitingForDispatch()).append(',');
		builder.append("\"tools\":");
		appendTools(builder, toolGroup.getTools());
		builder.append(',');
		builder.append("\"queued_items\":");
		appendQueuedItems(builder, toolGroup.getQueuedItems());
		builder.append(',');
		builder.append("\"in_process_items\":");
		appendInProcessItems(builder, toolGroup.getInProcessItems());
		builder.append('}');
	}

	private void appendTools(final StringBuilder builder, final List<ToolSnapshot> tools) {
		builder.append('[');
		for (int i = 0; i < tools.size(); i++) {
			if (i > 0) {
				builder.append(',');
			}
			final ToolSnapshot tool = tools.get(i);
			builder.append('{');
			builder.append("\"id\":").append(tool.getId()).append(',');
			builder.append("\"current_tool_state\":").append(quote(tool.getCurrentToolState()));
			builder.append('}');
		}
		builder.append(']');
	}

	private void appendQueuedItems(final StringBuilder builder, final List<FlowItemQueuedSnapshot> items) {
		builder.append('[');
		for (int i = 0; i < items.size(); i++) {
			if (i > 0) {
				builder.append(',');
			}
			appendFlowItemQueued(builder, items.get(i));
		}
		builder.append(']');
	}

	private void appendInProcessItems(final StringBuilder builder, final List<FlowItemInProcessSnapshot> items) {
		builder.append('[');
		for (int i = 0; i < items.size(); i++) {
			if (i > 0) {
				builder.append(',');
			}
			final FlowItemInProcessSnapshot item = items.get(i);
			builder.append('{');
			builder.append("\"remaining_cycle_time\":").append(item.getRemainingCycleTime()).append(',');
			builder.append("\"processing_time_left\":").append(item.getProcessingTimeLeft()).append(',');
			builder.append("\"priority\":").append(item.getPriority()).append(',');
			builder.append("\"lateness\":").append(item.getLateness());
			builder.append('}');
		}
		builder.append(']');
	}

	private void appendFlowItemQueued(final StringBuilder builder, final FlowItemQueuedSnapshot item) {
		builder.append('{');
		builder.append("\"remaining_cycle_time\":").append(item.getRemainingCycleTime()).append(',');
		builder.append("\"processing_time\":").append(item.getProcessingTime()).append(',');
		builder.append("\"expected_setup_time\":").append(item.getExpectedSetupTime()).append(',');
		builder.append("\"time_since_arrival\":").append(item.getTimeSinceArrival()).append(',');
		builder.append("\"priority\":").append(item.getPriority()).append(',');
		builder.append("\"lateness\":").append(item.getLateness()).append(',');
		builder.append("\"recipe\":").append(quote(item.getRecipe()));
		builder.append('}');
	}

	private String quote(final String value) {
		if (value == null) {
			return "\"\"";
		}
		final StringBuilder escaped = new StringBuilder(value.length() + 2);
		escaped.append('"');
		for (int i = 0; i < value.length(); i++) {
			final char c = value.charAt(i);
			switch (c) {
			case '\\':
				escaped.append("\\\\");
				break;
			case '"':
				escaped.append("\\\"");
				break;
			case '\b':
				escaped.append("\\b");
				break;
			case '\f':
				escaped.append("\\f");
				break;
			case '\n':
				escaped.append("\\n");
				break;
			case '\r':
				escaped.append("\\r");
				break;
			case '\t':
				escaped.append("\\t");
				break;
			default:
				if (c < 0x20) {
					escaped.append(String.format("\\u%04x", (int) c));
				} else {
					escaped.append(c);
				}
				break;
			}
		}
		escaped.append('"');
		return escaped.toString();
	}

}
