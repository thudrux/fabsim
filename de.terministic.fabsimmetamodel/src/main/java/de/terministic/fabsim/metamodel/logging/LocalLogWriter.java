package de.terministic.fabsim.metamodel.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import de.terministic.fabsim.externaldispatch.grpc.FabStateSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemInProcessSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemQueuedSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.ToolGroupSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.ToolSnapshot;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionSnapshot;

public final class LocalLogWriter {

	private final Path logFile;
	private final List<DispatchDecisionLogEntry> dispatchDecisions = new ArrayList<>();
	private final List<SinkArrivalLogEntry> sinkArrivals = new ArrayList<>();

	public LocalLogWriter(final Path logFile) {
		this.logFile = logFile;
	}

	public synchronized void append(final DispatchDecisionSnapshot snapshot, final AbstractFlowItem selectedFlowItem) {
		if (snapshot == null || selectedFlowItem == null) {
			return;
		}
		final FlowItemQueuedSnapshot chosenFlowItem = DispatchDecisionSnapshot.FlowItemQueuedSnapshotDto
				.capture(selectedFlowItem, snapshot.getSimulationTime()).toProto();
		this.dispatchDecisions.add(new DispatchDecisionLogEntry(snapshot.toFabStateSnapshot(), chosenFlowItem));
		writeLogFile();
	}

	public synchronized void appendSinkArrival(final long sinkArrivalTime, final Lot lot) {
		if (lot == null) {
			return;
		}
		this.sinkArrivals.add(new SinkArrivalLogEntry(sinkArrivalTime,
				lot.getProduct() == null ? "" : lot.getProduct().getName(), lot.getPrio(), lot.getDueDate()));
		writeLogFile();
	}

	private void writeLogFile() {
		if (this.logFile == null) {
			return;
		}
		try {
			final Path parent = this.logFile.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.write(this.logFile, toJson().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (final IOException ex) {
			throw new IllegalStateException("Failed to write dispatch decision log to " + this.logFile, ex);
		}
	}

	private String toJson() {
		final StringBuilder builder = new StringBuilder();
		builder.append('{');
		builder.append("\"dispatch_decisions\":");
		appendDispatchDecisions(builder, this.dispatchDecisions);
		builder.append(',');
		builder.append("\"sink_arrivals\":");
		appendSinkArrivals(builder, this.sinkArrivals);
		builder.append('}');
		return builder.toString();
	}

	private void appendDispatchDecisions(final StringBuilder builder, final List<DispatchDecisionLogEntry> entries) {
		builder.append('[');
		for (int i = 0; i < entries.size(); i++) {
			if (i > 0) {
				builder.append(',');
			}
			appendDispatchDecisionEntry(builder, entries.get(i));
		}
		builder.append(']');
	}

	private void appendSinkArrivals(final StringBuilder builder, final List<SinkArrivalLogEntry> entries) {
		builder.append('[');
		for (int i = 0; i < entries.size(); i++) {
			if (i > 0) {
				builder.append(',');
			}
			appendSinkArrival(builder, entries.get(i));
		}
		builder.append(']');
	}

	private void appendDispatchDecisionEntry(final StringBuilder builder, final DispatchDecisionLogEntry entry) {
		builder.append('{');
		builder.append("\"fab_state\":");
		appendFabState(builder, entry.getFabState());
		builder.append(',');
		builder.append("\"dispatch_decision\":");
		appendDispatchDecision(builder, entry.getDispatchDecision());
		builder.append('}');
	}

	private void appendDispatchDecision(final StringBuilder builder, final FlowItemQueuedSnapshot chosenFlowItem) {
		builder.append('{');
		builder.append("\"chosen_flow_item\":");
		appendFlowItemQueued(builder, chosenFlowItem);
		builder.append('}');
	}

	private void appendSinkArrival(final StringBuilder builder, final SinkArrivalLogEntry entry) {
		builder.append('{');
		builder.append("\"sink_arrival_time\":").append(entry.getSinkArrivalTime()).append(',');
		builder.append("\"product_name\":").append(quote(entry.getProductName())).append(',');
		builder.append("\"priority\":").append(entry.getPriority()).append(',');
		builder.append("\"due_date\":").append(entry.getDueDate());
		builder.append('}');
	}

	private void appendFabState(final StringBuilder builder, final FabStateSnapshot fabState) {
		builder.append('{');
		builder.append("\"simulation_time\":").append(fabState.getSimulationTime()).append(',');
		builder.append("\"tool_groups\":");
		builder.append('[');
		final List<ToolGroupSnapshot> toolGroups = fabState.getToolGroupsList();
		for (int i = 0; i < toolGroups.size(); i++) {
			if (i > 0) {
				builder.append(',');
			}
			appendToolGroup(builder, toolGroups.get(i));
		}
		builder.append(']');
		builder.append('}');
	}

	private void appendToolGroup(final StringBuilder builder, final ToolGroupSnapshot toolGroup) {
		builder.append('{');
		builder.append("\"name\":").append(quote(toolGroup.getName())).append(',');
		builder.append("\"waiting_for_dispatch\":").append(toolGroup.getWaitingForDispatch()).append(',');
		builder.append("\"tools\":");
		appendTools(builder, toolGroup.getToolsList());
		builder.append(',');
		builder.append("\"queued_items\":");
		appendQueuedItems(builder, toolGroup.getQueuedItemsList());
		builder.append(',');
		builder.append("\"in_process_items\":");
		appendInProcessItems(builder, toolGroup.getInProcessItemsList());
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
		builder.append("\"lateness\":").append(item.getLateness());
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

	private static final class DispatchDecisionLogEntry {
		private final FabStateSnapshot fabState;
		private final FlowItemQueuedSnapshot dispatchDecision;

		private DispatchDecisionLogEntry(final FabStateSnapshot fabState, final FlowItemQueuedSnapshot dispatchDecision) {
			this.fabState = fabState;
			this.dispatchDecision = dispatchDecision;
		}

		private FabStateSnapshot getFabState() {
			return this.fabState;
		}

		private FlowItemQueuedSnapshot getDispatchDecision() {
			return this.dispatchDecision;
		}
	}

	private static final class SinkArrivalLogEntry {
		private final long sinkArrivalTime;
		private final String productName;
		private final int priority;
		private final long dueDate;

		private SinkArrivalLogEntry(final long sinkArrivalTime, final String productName, final int priority,
				final long dueDate) {
			this.sinkArrivalTime = sinkArrivalTime;
			this.productName = productName;
			this.priority = priority;
			this.dueDate = dueDate;
		}

		private long getSinkArrivalTime() {
			return this.sinkArrivalTime;
		}

		private String getProductName() {
			return this.productName;
		}

		private int getPriority() {
			return this.priority;
		}

		private long getDueDate() {
			return this.dueDate;
		}
	}
}
