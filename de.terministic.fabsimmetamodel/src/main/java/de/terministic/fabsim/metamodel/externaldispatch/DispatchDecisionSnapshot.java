package de.terministic.fabsim.metamodel.externaldispatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;

import de.terministic.fabsim.externaldispatch.grpc.DispatchDecisionRequest;
import de.terministic.fabsim.externaldispatch.grpc.FabStateSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemInProcessSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemQueuedSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemQueuedWithIDSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.ToolGroupSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.ToolSnapshot;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

public final class DispatchDecisionSnapshot {

	private final long simulationTime;
	private final List<ToolGroupSnapshotDto> toolGroups;
	private final List<FlowItemQueuedWithIDSnapshotDto> candidates;

	private DispatchDecisionSnapshot(final long simulationTime, final List<ToolGroupSnapshotDto> toolGroups,
			final List<FlowItemQueuedWithIDSnapshotDto> candidates) {
		this.simulationTime = simulationTime;
		this.toolGroups = toolGroups;
		this.candidates = candidates;
	}

	public long getSimulationTime() {
		return this.simulationTime;
	}

	public static DispatchDecisionSnapshot capture(final FabModel model, final AbstractToolGroup toolGroup,
			final AbstractTool tool, final Collection<AbstractFlowItem> candidates, final String dispatchRuleName) {
		final long simulationTime = model.getSimulationEngine() == null ? 0L : model.getSimulationEngine().getTime();
		final List<ToolGroupSnapshotDto> toolGroups = new ArrayList<>();
		for (final AbstractToolGroup groupBase : model.getToolGroups().values()) {
			toolGroups.add(ToolGroupSnapshotDto.capture(groupBase, groupBase.getId() == toolGroup.getId(),
					simulationTime));
		}
		final List<FlowItemQueuedWithIDSnapshotDto> candidateSnapshots = new ArrayList<>();
		for (final AbstractFlowItem item : candidates) {
			candidateSnapshots.add(FlowItemQueuedWithIDSnapshotDto.capture(item, simulationTime));
		}
		return new DispatchDecisionSnapshot(simulationTime, toolGroups, candidateSnapshots);
	}

	public DispatchDecisionRequest toProto() {
		final DispatchDecisionRequest.Builder builder = DispatchDecisionRequest.newBuilder()
				.setFabState(toFabStateSnapshot());
		for (final FlowItemQueuedWithIDSnapshotDto candidate : this.candidates) {
			builder.addCandidates(candidate.toWithIdProto());
		}
		return builder.build();
	}

	public FabStateSnapshot toFabStateSnapshot() {
		final FabStateSnapshot.Builder builder = FabStateSnapshot.newBuilder().setSimulationTime(this.simulationTime);
		for (final ToolGroupSnapshotDto toolGroup : this.toolGroups) {
			builder.addToolGroups(toolGroup.toProto());
		}
		return builder.build();
	}

	public static final class ToolGroupSnapshotDto {
		private final String name;
		private final boolean waitingForDispatch;
		private final List<ToolSnapshotDto> tools;
		private final List<FlowItemQueuedSnapshotDto> queuedItems;
		private final List<FlowItemInProcessSnapshotDto> inProcessItems;

		private ToolGroupSnapshotDto(final String name, final boolean waitingForDispatch,
				final List<ToolSnapshotDto> tools, final List<FlowItemQueuedSnapshotDto> queuedItems,
				final List<FlowItemInProcessSnapshotDto> inProcessItems) {
			this.name = name;
			this.waitingForDispatch = waitingForDispatch;
			this.tools = tools;
			this.queuedItems = queuedItems;
			this.inProcessItems = inProcessItems;
		}

		public static ToolGroupSnapshotDto capture(final AbstractToolGroup toolGroupBase,
				final boolean waitingForDispatch, final long currentTime) {
			final ToolGroup toolGroup = (ToolGroup) toolGroupBase;
			final List<ToolSnapshotDto> tools = new ArrayList<>();
			for (final AbstractTool tool : toolGroup.getTools().values()) {
				tools.add(ToolSnapshotDto.capture(tool));
			}
			final List<FlowItemQueuedSnapshotDto> queuedItems = new ArrayList<>();
			for (final AbstractFlowItem item : toolGroup.getQueue()) {
				queuedItems.add(FlowItemQueuedSnapshotDto.capture(item, currentTime));
			}
			final List<FlowItemInProcessSnapshotDto> inProcessItems = new ArrayList<>();
			for (final Map.Entry<AbstractFlowItem, AbstractTool> entry : toolGroup.getInProcessMap().entrySet()) {
				inProcessItems.add(FlowItemInProcessSnapshotDto.capture(entry.getKey(), entry.getValue(), currentTime));
			}
			return new ToolGroupSnapshotDto(toolGroup.getName(), waitingForDispatch, tools, queuedItems, inProcessItems);
		}

		public ToolGroupSnapshot toProto() {
			final ToolGroupSnapshot.Builder builder = ToolGroupSnapshot.newBuilder()
					.setName(this.name)
					.setWaitingForDispatch(this.waitingForDispatch);
			for (final ToolSnapshotDto tool : this.tools) {
				builder.addTools(tool.toProto());
			}
			for (final FlowItemQueuedSnapshotDto item : this.queuedItems) {
				builder.addQueuedItems(item.toProto());
			}
			for (final FlowItemInProcessSnapshotDto item : this.inProcessItems) {
				builder.addInProcessItems(item.toProto());
			}
			return builder.build();
		}
	}

	public static final class ToolSnapshotDto {
		private final long id;
		private final String currentToolState;

		private ToolSnapshotDto(final long id, final String currentToolState) {
			this.id = id;
			this.currentToolState = currentToolState;
		}

		public static ToolSnapshotDto capture(final AbstractTool tool) {
			return new ToolSnapshotDto(tool.getId(),
					tool.getCurrentToolState() == null ? "" : tool.getCurrentToolState().name());
		}

		public ToolSnapshot toProto() {
			return ToolSnapshot.newBuilder()
					.setId(this.id)
					.setCurrentToolState(this.currentToolState)
					.build();
		}
	}

	private abstract static class FlowItemSnapshotBaseDto {
		private final String productName;
		private final int currentStepNumber;
		private final int priority;
		private final long lateness;

		private FlowItemSnapshotBaseDto(final String productName, final int currentStepNumber, final int priority,
				final long lateness) {
			this.productName = productName;
			this.currentStepNumber = currentStepNumber;
			this.priority = priority;
			this.lateness = lateness;
		}

		protected String getProductName() {
			return this.productName;
		}

		protected int getCurrentStepNumber() {
			return this.currentStepNumber;
		}

		protected int getPriority() {
			return this.priority;
		}

		protected long getLateness() {
			return this.lateness;
		}
	}

	public static class FlowItemQueuedSnapshotDto extends FlowItemSnapshotBaseDto {
		private final long timeSinceArrival;

		private FlowItemQueuedSnapshotDto(final String productName, final int currentStepNumber,
				final long timeSinceArrival, final int priority, final long lateness) {
			super(productName, currentStepNumber, priority, lateness);
			this.timeSinceArrival = timeSinceArrival;
		}

		public static FlowItemQueuedSnapshotDto capture(final AbstractFlowItem item, final long currentTime) {
			return new FlowItemQueuedSnapshotDto(resolveProductName(item),
					item.getCurrentStepNumber(), calculateTimeSinceArrival(item, currentTime),
					calculatePriority(item), calculateLateness(item, currentTime));
		}

		public FlowItemQueuedSnapshot toProto() {
			return FlowItemQueuedSnapshot.newBuilder()
					.setProductName(getProductName())
					.setCurrentStepNumber(getCurrentStepNumber())
					.setTimeSinceArrival(getTimeSinceArrival())
					.setPriority(getPriority())
					.setLateness(getLateness())
					.build();
		}

		protected long getTimeSinceArrival() {
			return this.timeSinceArrival;
		}
	}

	public static final class FlowItemInProcessSnapshotDto extends FlowItemSnapshotBaseDto {
		private final long processingTimeLeft;

		private FlowItemInProcessSnapshotDto(final String productName, final int currentStepNumber,
				final long processingTimeLeft, final int priority, final long lateness) {
			super(productName, currentStepNumber, priority, lateness);
			this.processingTimeLeft = processingTimeLeft;
		}

		public static FlowItemInProcessSnapshotDto capture(final AbstractFlowItem item, final AbstractTool tool,
				final long currentTime) {
			final long remainingProcessTime = tool.getToolStateMachine().getRemainingProcessTime(tool);
			return new FlowItemInProcessSnapshotDto(resolveProductName(item),
					item.getCurrentStepNumber(), Math.max(0L, remainingProcessTime), calculatePriority(item),
					calculateLateness(item, currentTime));
		}

		public FlowItemInProcessSnapshot toProto() {
			return FlowItemInProcessSnapshot.newBuilder()
					.setProductName(getProductName())
					.setCurrentStepNumber(getCurrentStepNumber())
					.setProcessingTimeLeft(this.processingTimeLeft)
					.setPriority(getPriority())
					.setLateness(getLateness())
					.build();
		}
	}

	public static final class FlowItemQueuedWithIDSnapshotDto extends FlowItemQueuedSnapshotDto {
		private final long id;

		private FlowItemQueuedWithIDSnapshotDto(final long id, final String productName, final int currentStepNumber,
				final long timeSinceArrival, final int priority, final long lateness) {
			super(productName, currentStepNumber, timeSinceArrival, priority, lateness);
			this.id = id;
		}

		public static FlowItemQueuedWithIDSnapshotDto capture(final AbstractFlowItem item, final long currentTime) {
			return new FlowItemQueuedWithIDSnapshotDto(item.getId(), resolveProductName(item),
					item.getCurrentStepNumber(), calculateTimeSinceArrival(item, currentTime),
					calculatePriority(item), calculateLateness(item, currentTime));
		}

		public FlowItemQueuedWithIDSnapshot toWithIdProto() {
			return FlowItemQueuedWithIDSnapshot.newBuilder()
					.setId(this.id)
					.setProductName(getProductName())
					.setCurrentStepNumber(getCurrentStepNumber())
					.setTimeSinceArrival(getTimeSinceArrival())
					.setPriority(getPriority())
					.setLateness(getLateness())
					.build();
		}
	}

	private static long calculateTimeSinceArrival(final AbstractFlowItem item, final long currentTime) {
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return Math.max(0L, currentTime - item.getTimeStamps(item.getCurrentStepNumber()).getArrivalTime());
			}
			long totalTimeSinceArrival = 0L;
			for (final AbstractFlowItem lot : batch.getItems()) {
				totalTimeSinceArrival += currentTime - lot.getTimeStamps(lot.getCurrentStepNumber()).getArrivalTime();
			}
			return Math.round(totalTimeSinceArrival / (double) batch.getItems().size());
		}
		return Math.max(0L, currentTime - item.getTimeStamps(item.getCurrentStepNumber()).getArrivalTime());
	}

	private static int calculatePriority(final AbstractFlowItem item) {
		if (item instanceof Lot) {
			return ((Lot) item).getPrio();
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return Integer.MAX_VALUE;
			}
			long totalPriority = 0L;
			for (final AbstractFlowItem child : batch.getItems()) {
				totalPriority += calculatePriority(child);
			}
			return Math.toIntExact(Math.round(totalPriority / (double) batch.getItems().size()));
		}
		return Integer.MAX_VALUE;
	}

	private static long calculateLateness(final AbstractFlowItem item, final long currentTime) {
		if (item instanceof Lot) {
			return currentTime - ((Lot) item).getDueDate();
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return Long.MIN_VALUE;
			}
			long totalLateness = 0L;
			for (final AbstractFlowItem child : batch.getItems()) {
				totalLateness += calculateLateness(child, currentTime);
			}
			return Math.round(totalLateness / (double) batch.getItems().size());
		}
		return Long.MIN_VALUE;
	}

	private static String resolveProductName(final AbstractFlowItem item) {
		if (item == null) {
			return "";
		}
		final Product product = item.getProduct();
		if (product != null) {
			return product.getName();
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (!batch.getItems().isEmpty()) {
				final AbstractFlowItem firstChild = batch.getItems().get(0);
				if (firstChild != null && firstChild.getProduct() != null) {
					return firstChild.getProduct().getName();
				}
			}
		}
		return "";
	}
}
