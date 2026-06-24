package de.terministic.fabsim.metamodel.externaldispatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import de.terministic.fabsim.externaldispatch.grpc.DispatchDecisionRequest;
import de.terministic.fabsim.externaldispatch.grpc.FabStateSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.CostSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemInProcessSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemQueuedSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.FlowItemQueuedWithIDSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.ToolGroupSnapshot;
import de.terministic.fabsim.externaldispatch.grpc.ToolSnapshot;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.examples.MiniFab;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
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
			candidateSnapshots.add(FlowItemQueuedWithIDSnapshotDto.capture(item, tool, simulationTime));
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
		long totalProjectedTardiness = 0L;
		long workInProgress = 0L;
		final FabStateSnapshot.Builder builder = FabStateSnapshot.newBuilder().setSimulationTime(this.simulationTime);
		for (final ToolGroupSnapshotDto toolGroup : this.toolGroups) {
			builder.addToolGroups(toolGroup.toProto());
			totalProjectedTardiness += toolGroup.getTotalProjectedTardiness();
			workInProgress += toolGroup.getWorkInProgress();
		}
		builder.setCostSnapshot(CostSnapshot.newBuilder()
				.setTotalProjectedTardiness(totalProjectedTardiness)
				.setWorkInProgress(workInProgress)
				.build());
		return builder.build();
	}

	public static final class ToolGroupSnapshotDto {
		private final String name;
		private final boolean waitingForDispatch;
		private final List<ToolSnapshotDto> tools;
		private final List<FlowItemQueuedSnapshotDto> queuedItems;
		private final List<FlowItemInProcessSnapshotDto> inProcessItems;
		private final long totalProjectedTardiness;
		private final long workInProgress;

		private ToolGroupSnapshotDto(final String name, final boolean waitingForDispatch,
				final List<ToolSnapshotDto> tools, final List<FlowItemQueuedSnapshotDto> queuedItems,
				final List<FlowItemInProcessSnapshotDto> inProcessItems, final long totalProjectedTardiness,
				final long workInProgress) {
			this.name = name;
			this.waitingForDispatch = waitingForDispatch;
			this.tools = tools;
			this.queuedItems = queuedItems;
			this.inProcessItems = inProcessItems;
			this.totalProjectedTardiness = totalProjectedTardiness;
			this.workInProgress = workInProgress;
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
				queuedItems.add(FlowItemQueuedSnapshotDto.capture(item, toolGroup, currentTime));
			}
			final List<FlowItemInProcessSnapshotDto> inProcessItems = new ArrayList<>();
			final Map<AbstractTool, AbstractFlowItem> itemByTool = new LinkedHashMap<>();
			for (final Map.Entry<AbstractFlowItem, AbstractTool> entry : toolGroup.getInProcessMap().entrySet()) {
				itemByTool.putIfAbsent(entry.getValue(), entry.getKey());
			}
			// A tool group cannot process more items in parallel than it has tools.
			// Building the snapshot by tool keeps the log aligned with the physical capacity
			// and ignores any stale duplicate bookkeeping entries.
			for (final AbstractTool tool : toolGroup.getTools().values()) {
				final AbstractFlowItem item = itemByTool.get(tool);
				if (item != null) {
					inProcessItems.add(FlowItemInProcessSnapshotDto.capture(item, tool, currentTime));
				}
			}
			long totalProjectedTardiness = 0L;
			for (final AbstractFlowItem item : toolGroup.getQueue()) {
				totalProjectedTardiness += calculateWaferLevelProjectedTardiness(item, currentTime);
			}
			for (final AbstractFlowItem item : itemByTool.values()) {
				totalProjectedTardiness += calculateWaferLevelProjectedTardiness(item, currentTime);
			}
			long workInProgress = 0L;
			for (final AbstractFlowItem item : toolGroup.getQueue()) {
				workInProgress += calculateWaferLevelWorkInProgress(item);
			}
			for (final AbstractFlowItem item : itemByTool.values()) {
				workInProgress += calculateWaferLevelWorkInProgress(item);
			}
			return new ToolGroupSnapshotDto(toolGroup.getName(), waitingForDispatch, tools, queuedItems, inProcessItems,
					totalProjectedTardiness, workInProgress);
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

		protected long getTotalProjectedTardiness() {
			return this.totalProjectedTardiness;
		}

		protected long getWorkInProgress() {
			return this.workInProgress;
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
			return new ToolSnapshotDto(tool.getId(), tool.getCurrentToolState() == null ? ""
					: tool.getCurrentToolState().name());
		}

		public ToolSnapshot toProto() {
			return ToolSnapshot.newBuilder()
					.setId(this.id)
					.setCurrentToolState(this.currentToolState)
					.build();
		}
	}

	private abstract static class FlowItemSnapshotBaseDto {
		private final int priority;
		private final long lateness;
		private final String recipe;

		private FlowItemSnapshotBaseDto(final int priority, final long lateness, final String recipe) {
			this.priority = priority;
			this.lateness = lateness;
			this.recipe = recipe;
		}

		protected int getPriority() {
			return this.priority;
		}

		protected long getLateness() {
			return this.lateness;
		}

		protected String getRecipe() {
			return this.recipe;
		}
	}

	public static class FlowItemQueuedSnapshotDto extends FlowItemSnapshotBaseDto {
		private final long remainingCycleTime;
		private final long processingTime;
		private final long expectedSetupTime;
		private final long timeSinceArrival;

		private FlowItemQueuedSnapshotDto(final long remainingCycleTime, final long processingTime,
				final long expectedSetupTime, final long timeSinceArrival, final int priority,
				final long lateness, final String recipe) {
			super(priority, lateness, recipe);
			this.remainingCycleTime = remainingCycleTime;
			this.processingTime = processingTime;
			this.expectedSetupTime = expectedSetupTime;
			this.timeSinceArrival = timeSinceArrival;
		}

		public static FlowItemQueuedSnapshotDto capture(final AbstractFlowItem item, final ToolGroup toolGroup,
				final long currentTime) {
			return new FlowItemQueuedSnapshotDto(calculateRemainingCycleTime(item), calculateProcessingTime(item),
					calculateExpectedSetupTime(toolGroup, item), calculateTimeSinceArrival(item, currentTime),
					calculatePriority(item), calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public static FlowItemQueuedSnapshotDto capture(final AbstractFlowItem item, final long currentTime) {
			return new FlowItemQueuedSnapshotDto(calculateRemainingCycleTime(item), calculateProcessingTime(item), 0L,
					calculateTimeSinceArrival(item, currentTime), calculatePriority(item),
					calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public static FlowItemQueuedSnapshotDto capture(final AbstractFlowItem item, final AbstractTool tool,
				final long currentTime) {
			return new FlowItemQueuedSnapshotDto(calculateRemainingCycleTime(item), calculateProcessingTime(item),
					calculateExpectedSetupTime(tool, item), calculateTimeSinceArrival(item, currentTime),
					calculatePriority(item), calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public FlowItemQueuedSnapshot toProto() {
			return FlowItemQueuedSnapshot.newBuilder()
					.setRemainingCycleTime(this.remainingCycleTime)
					.setProcessingTime(this.processingTime)
					.setExpectedSetupTime(this.expectedSetupTime)
					.setTimeSinceArrival(getTimeSinceArrival())
					.setPriority(getPriority())
					.setLateness(getLateness())
					.setRecipe(getRecipe())
					.build();
		}

		protected long getRemainingCycleTime() {
			return this.remainingCycleTime;
		}

		protected long getProcessingTime() {
			return this.processingTime;
		}

		protected long getExpectedSetupTime() {
			return this.expectedSetupTime;
		}

		protected long getTimeSinceArrival() {
			return this.timeSinceArrival;
		}
	}

	public static final class FlowItemInProcessSnapshotDto extends FlowItemSnapshotBaseDto {
		private final long remainingCycleTime;
		private final long processingTimeLeft;

		private FlowItemInProcessSnapshotDto(final long remainingCycleTime, final long processingTimeLeft,
				final int priority, final long lateness) {
			super(priority, lateness, "");
			this.remainingCycleTime = remainingCycleTime;
			this.processingTimeLeft = processingTimeLeft;
		}

		public static FlowItemInProcessSnapshotDto capture(final AbstractFlowItem item, final AbstractTool tool,
				final long currentTime) {
			final long remainingProcessTime = tool.getToolStateMachine().getRemainingProcessTime(tool);
			final long processingTimeLeft = Math.max(0L, remainingProcessTime);
			return new FlowItemInProcessSnapshotDto(
					calculateRemainingCycleTime(item, processingTimeLeft),
					processingTimeLeft,
					calculatePriority(item),
					calculateLateness(item, currentTime));
		}

		public FlowItemInProcessSnapshot toProto() {
			return FlowItemInProcessSnapshot.newBuilder()
					.setRemainingCycleTime(this.remainingCycleTime)
					.setProcessingTimeLeft(this.processingTimeLeft)
					.setPriority(getPriority())
					.setLateness(getLateness())
					.build();
		}

		protected long getRemainingCycleTime() {
			return this.remainingCycleTime;
		}
	}

	public static final class FlowItemQueuedWithIDSnapshotDto extends FlowItemQueuedSnapshotDto {
		private final long id;

		private FlowItemQueuedWithIDSnapshotDto(final long id, final long remainingCycleTime,
				final long processingTime, final long expectedSetupTime, final long timeSinceArrival,
				final int priority, final long lateness, final String recipe) {
			super(remainingCycleTime, processingTime, expectedSetupTime, timeSinceArrival, priority, lateness,
					recipe);
			this.id = id;
		}

		public static FlowItemQueuedWithIDSnapshotDto capture(final AbstractFlowItem item, final AbstractTool tool,
				final long currentTime) {
			return new FlowItemQueuedWithIDSnapshotDto(item.getId(), calculateRemainingCycleTime(item),
					calculateProcessingTime(item), calculateExpectedSetupTime(tool, item),
					calculateTimeSinceArrival(item, currentTime), calculatePriority(item),
					calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public FlowItemQueuedWithIDSnapshot toWithIdProto() {
			return FlowItemQueuedWithIDSnapshot.newBuilder()
					.setId(this.id)
					.setRemainingCycleTime(getRemainingCycleTime())
					.setProcessingTime(getProcessingTime())
					.setExpectedSetupTime(getExpectedSetupTime())
					.setTimeSinceArrival(getTimeSinceArrival())
					.setPriority(getPriority())
					.setLateness(getLateness())
					.setRecipe(getRecipe())
					.build();
		}
	}

	private static long calculateProcessingTime(final AbstractFlowItem item) {
		if (item == null || item.getRecipe() == null) {
			return 0L;
		}
		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return 0L;
		}
		return Math.max(0L, item.getRecipe().get(currentStepNumber).getDuration(item));
	}

	private static long calculateRemainingCycleTime(final AbstractFlowItem item) {
		if (item == null || item.getRecipe() == null) {
			return 0L;
		}
		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return 0L;
		}
		long remainingProcessTime = 0L;
		for (int i = currentStepNumber; i < item.getRecipe().size(); i++) {
			remainingProcessTime += calculateStepCycleTime(item, item.getRecipe().get(i));
		}
		return Math.round(remainingProcessTime * MiniFab.FLOW_FACTOR);
	}

	private static long calculateRemainingCycleTime(final AbstractFlowItem item, final long processingTimeLeft) {
		if (item == null || item.getRecipe() == null) {
			return Math.max(0L, processingTimeLeft);
		}
		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return Math.max(0L, processingTimeLeft);
		}
		long futureProcessTime = 0L;
		for (int i = currentStepNumber + 1; i < item.getRecipe().size(); i++) {
			futureProcessTime += calculateStepCycleTime(item, item.getRecipe().get(i));
		}
		return Math.round((processingTimeLeft + futureProcessTime) * MiniFab.FLOW_FACTOR);
	}

	private static long calculateWaferLevelProjectedTardiness(final AbstractFlowItem item, final long currentTime) {
		if (item == null) {
			return 0L;
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return 0L;
			}
			long total = 0L;
			for (final AbstractFlowItem lot : batch.getItems()) {
				total += calculateWaferLevelProjectedTardiness(lot, currentTime);
			}
			return total;
		}
		return item.getSize() * (calculateLateness(item, currentTime) + calculateRemainingCycleTime(item));
	}

	private static long calculateWaferLevelWorkInProgress(final AbstractFlowItem item) {
		if (item == null) {
			return 0L;
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return 0L;
			}
			long total = 0L;
			for (final AbstractFlowItem lot : batch.getItems()) {
				total += calculateWaferLevelWorkInProgress(lot);
			}
			return total;
		}
		return Math.max(0L, item.getSize());
	}

	private static long calculateStepCycleTime(final AbstractFlowItem item, final ProcessStep step) {
		return Math.max(0L, step.getLoadTime()) + Math.max(0L, step.getDuration(item))
				+ Math.max(0L, step.getUnloadTime());
	}

	private static long calculateExpectedSetupTime(final AbstractTool tool, final AbstractFlowItem item) {
		if (tool == null || item == null || item.getRecipe() == null) {
			return 0L;
		}
		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return 0L;
		}
		final SetupState desiredState = item.getRecipe().get(currentStepNumber).getSetupDetails();
		if (desiredState == null) {
			return 0L;
		}
		final SetupState currentState = tool.getCurrentSetupState();
		if (currentState == null || currentState.equals(desiredState)) {
			return 0L;
		}
		final Map<SetupState, Long> transitions = tool.getSetupTransitions().get(currentState);
		if (transitions == null) {
			return 0L;
		}
		final Long transitionTime = transitions.get(desiredState);
		return transitionTime == null ? 0L : Math.max(0L, transitionTime);
	}

	private static long calculateExpectedSetupTime(final ToolGroup toolGroup, final AbstractFlowItem item) {
		if (toolGroup == null) {
			return 0L;
		}
		final List<AbstractTool> candidateTools = new ArrayList<>();
		if (toolGroup.getStandbyTools() != null && !toolGroup.getStandbyTools().isEmpty()) {
			candidateTools.addAll(toolGroup.getStandbyTools());
		} else {
			candidateTools.addAll(toolGroup.getTools().values());
		}
		long bestSetupTime = Long.MAX_VALUE;
		for (final AbstractTool tool : candidateTools) {
			bestSetupTime = Math.min(bestSetupTime, calculateExpectedSetupTime(tool, item));
		}
		return bestSetupTime == Long.MAX_VALUE ? 0L : bestSetupTime;
	}

	private static String calculateRecipe(final AbstractFlowItem item) {
		if (item == null || item.getRecipe() == null || item.getRecipe().getName() == null) {
			return "";
		}
		return item.getRecipe().getName();
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

}
