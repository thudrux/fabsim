package de.terministic.fabsim.metamodel.externaldispatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.examples.MiniFab;

public final class DispatchDecisionRequest {

	private final FabStateSnapshot fabState;
	private final List<FlowItemQueuedWithIDSnapshot> candidates;

	private DispatchDecisionRequest(final FabStateSnapshot fabState,
			final List<FlowItemQueuedWithIDSnapshot> candidates) {
		this.fabState = fabState;
		this.candidates = candidates;
	}

	public static DispatchDecisionRequest capture(final FabModel model, final AbstractToolGroup toolGroup,
			final AbstractTool tool, final Collection<AbstractFlowItem> candidates) {
		final long simulationTime = model == null || model.getSimulationEngine() == null ? 0L
				: model.getSimulationEngine().getTime();
		final FabStateSnapshot fabState = FabStateSnapshot.capture(model, toolGroup, simulationTime);
		final List<FlowItemQueuedWithIDSnapshot> candidateSnapshots = new ArrayList<>();
		if (candidates != null) {
			for (final AbstractFlowItem item : candidates) {
				candidateSnapshots.add(FlowItemQueuedWithIDSnapshot.capture(item, tool, simulationTime));
			}
		}
		return new DispatchDecisionRequest(fabState, Collections.unmodifiableList(candidateSnapshots));
	}

	public FabStateSnapshot getFabState() {
		return this.fabState;
	}

	public List<FlowItemQueuedWithIDSnapshot> getCandidates() {
		return this.candidates;
	}

	public long getSimulationTime() {
		return this.fabState == null ? 0L : this.fabState.getSimulationTime();
	}

	public static final class FabStateSnapshot {
		private final long simulationTime;
		private final List<ToolGroupSnapshot> toolGroups;
		private final CostSnapshot costSnapshot;

		private FabStateSnapshot(final long simulationTime, final List<ToolGroupSnapshot> toolGroups,
				final CostSnapshot costSnapshot) {
			this.simulationTime = simulationTime;
			this.toolGroups = toolGroups;
			this.costSnapshot = costSnapshot;
		}

		public static FabStateSnapshot capture(final FabModel model, final AbstractToolGroup selectedToolGroup,
				final long currentTime) {
			final List<ToolGroupSnapshot> toolGroups = new ArrayList<>();
			long totalProjectedTardiness = 0L;
			long workInProgress = 0L;
			if (model != null) {
				for (final AbstractToolGroup groupBase : model.getToolGroups().values()) {
					final boolean waitingForDispatch = selectedToolGroup != null
							&& groupBase.getId() == selectedToolGroup.getId();
					final ToolGroupSnapshot toolGroup = ToolGroupSnapshot.capture(groupBase, waitingForDispatch,
							currentTime);
					toolGroups.add(toolGroup);
					totalProjectedTardiness += toolGroup.totalProjectedTardiness;
					workInProgress += toolGroup.workInProgress;
				}
			}
			return new FabStateSnapshot(currentTime, Collections.unmodifiableList(toolGroups),
					new CostSnapshot(totalProjectedTardiness, workInProgress));
		}

		public long getSimulationTime() {
			return this.simulationTime;
		}

		public List<ToolGroupSnapshot> getToolGroups() {
			return this.toolGroups;
		}

		public CostSnapshot getCostSnapshot() {
			return this.costSnapshot;
		}
	}

	public static final class CostSnapshot {
		private final long totalProjectedTardiness;
		private final long workInProgress;

		private CostSnapshot(final long totalProjectedTardiness, final long workInProgress) {
			this.totalProjectedTardiness = totalProjectedTardiness;
			this.workInProgress = workInProgress;
		}

		public long getTotalProjectedTardiness() {
			return this.totalProjectedTardiness;
		}

		public long getWorkInProgress() {
			return this.workInProgress;
		}
	}

	public static final class ToolGroupSnapshot {
		private final String name;
		private final boolean waitingForDispatch;
		private final List<ToolSnapshot> tools;
		private final List<FlowItemQueuedSnapshot> queuedItems;
		private final List<FlowItemInProcessSnapshot> inProcessItems;
		private final long totalProjectedTardiness;
		private final long workInProgress;

		private ToolGroupSnapshot(final String name, final boolean waitingForDispatch, final List<ToolSnapshot> tools,
				final List<FlowItemQueuedSnapshot> queuedItems,
				final List<FlowItemInProcessSnapshot> inProcessItems, final long totalProjectedTardiness,
				final long workInProgress) {
			this.name = name;
			this.waitingForDispatch = waitingForDispatch;
			this.tools = tools;
			this.queuedItems = queuedItems;
			this.inProcessItems = inProcessItems;
			this.totalProjectedTardiness = totalProjectedTardiness;
			this.workInProgress = workInProgress;
		}

		public static ToolGroupSnapshot capture(final AbstractToolGroup toolGroupBase,
				final boolean waitingForDispatch, final long currentTime) {
			final ToolGroup toolGroup = (ToolGroup) toolGroupBase;
			final List<ToolSnapshot> tools = new ArrayList<>();
			for (final AbstractTool tool : toolGroup.getTools().values()) {
				tools.add(ToolSnapshot.capture(tool));
			}
			final List<FlowItemQueuedSnapshot> queuedItems = new ArrayList<>();
			for (final AbstractFlowItem item : toolGroup.getQueue()) {
				queuedItems.add(FlowItemQueuedSnapshot.capture(item, toolGroup, currentTime));
			}
			final List<FlowItemInProcessSnapshot> inProcessItems = new ArrayList<>();
			final Map<AbstractTool, AbstractFlowItem> itemByTool = new LinkedHashMap<>();
			for (final Map.Entry<AbstractFlowItem, AbstractTool> entry : toolGroup.getInProcessMap().entrySet()) {
				itemByTool.putIfAbsent(entry.getValue(), entry.getKey());
			}
			for (final AbstractTool tool : toolGroup.getTools().values()) {
				final AbstractFlowItem item = itemByTool.get(tool);
				if (item != null) {
					inProcessItems.add(FlowItemInProcessSnapshot.capture(item, tool, currentTime));
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
			return new ToolGroupSnapshot(toolGroup.getName(), waitingForDispatch,
					Collections.unmodifiableList(tools), Collections.unmodifiableList(queuedItems),
					Collections.unmodifiableList(inProcessItems), totalProjectedTardiness, workInProgress);
		}

		public String getName() {
			return this.name;
		}

		public boolean getWaitingForDispatch() {
			return this.waitingForDispatch;
		}

		public List<ToolSnapshot> getTools() {
			return this.tools;
		}

		public List<FlowItemQueuedSnapshot> getQueuedItems() {
			return this.queuedItems;
		}

		public List<FlowItemInProcessSnapshot> getInProcessItems() {
			return this.inProcessItems;
		}
	}

	public static final class ToolSnapshot {
		private final long id;
		private final String currentToolState;

		private ToolSnapshot(final long id, final String currentToolState) {
			this.id = id;
			this.currentToolState = currentToolState;
		}

		public static ToolSnapshot capture(final AbstractTool tool) {
			return new ToolSnapshot(tool.getId(), tool.getCurrentToolState() == null ? ""
					: tool.getCurrentToolState().name());
		}

		public long getId() {
			return this.id;
		}

		public String getCurrentToolState() {
			return this.currentToolState;
		}
	}

	private abstract static class FlowItemSnapshotBase {
		private final int priority;
		private final long lateness;
		private final String recipe;

		private FlowItemSnapshotBase(final int priority, final long lateness, final String recipe) {
			this.priority = priority;
			this.lateness = lateness;
			this.recipe = recipe;
		}

		public int getPriority() {
			return this.priority;
		}

		public long getLateness() {
			return this.lateness;
		}

		public String getRecipe() {
			return this.recipe;
		}
	}

	public static class FlowItemQueuedSnapshot extends FlowItemSnapshotBase {
		private final long remainingCycleTime;
		private final long processingTime;
		private final long expectedSetupTime;
		private final long timeSinceArrival;

		private FlowItemQueuedSnapshot(final long remainingCycleTime, final long processingTime,
				final long expectedSetupTime, final long timeSinceArrival, final int priority,
				final long lateness, final String recipe) {
			super(priority, lateness, recipe);
			this.remainingCycleTime = remainingCycleTime;
			this.processingTime = processingTime;
			this.expectedSetupTime = expectedSetupTime;
			this.timeSinceArrival = timeSinceArrival;
		}

		public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final ToolGroup toolGroup,
				final long currentTime) {
			return new FlowItemQueuedSnapshot(calculateRemainingCycleTime(item), calculateProcessingTime(item),
					calculateExpectedSetupTime(toolGroup, item), calculateTimeSinceArrival(item, currentTime),
					calculatePriority(item), calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final AbstractTool tool,
				final long currentTime) {
			return new FlowItemQueuedSnapshot(calculateRemainingCycleTime(item), calculateProcessingTime(item),
					calculateExpectedSetupTime(tool, item), calculateTimeSinceArrival(item, currentTime),
					calculatePriority(item), calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public static FlowItemQueuedSnapshot capture(final AbstractFlowItem item, final long currentTime) {
			return new FlowItemQueuedSnapshot(calculateRemainingCycleTime(item), calculateProcessingTime(item), 0L,
					calculateTimeSinceArrival(item, currentTime), calculatePriority(item),
					calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public long getRemainingCycleTime() {
			return this.remainingCycleTime;
		}

		public long getProcessingTime() {
			return this.processingTime;
		}

		public long getExpectedSetupTime() {
			return this.expectedSetupTime;
		}

		public long getTimeSinceArrival() {
			return this.timeSinceArrival;
		}
	}

	public static final class FlowItemInProcessSnapshot extends FlowItemSnapshotBase {
		private final long remainingCycleTime;
		private final long processingTimeLeft;

		private FlowItemInProcessSnapshot(final long remainingCycleTime, final long processingTimeLeft,
				final int priority, final long lateness) {
			super(priority, lateness, "");
			this.remainingCycleTime = remainingCycleTime;
			this.processingTimeLeft = processingTimeLeft;
		}

		public static FlowItemInProcessSnapshot capture(final AbstractFlowItem item, final AbstractTool tool,
				final long currentTime) {
			final long remainingProcessTime = tool.getToolStateMachine().getRemainingProcessTime(tool);
			final long processingTimeLeft = Math.max(0L, remainingProcessTime);
			return new FlowItemInProcessSnapshot(calculateRemainingCycleTime(item, processingTimeLeft),
					processingTimeLeft, calculatePriority(item), calculateLateness(item, currentTime));
		}

		public long getRemainingCycleTime() {
			return this.remainingCycleTime;
		}

		public long getProcessingTimeLeft() {
			return this.processingTimeLeft;
		}
	}

	public static final class FlowItemQueuedWithIDSnapshot extends FlowItemQueuedSnapshot {
		private final long id;

		private FlowItemQueuedWithIDSnapshot(final long id, final long remainingCycleTime,
				final long processingTime, final long expectedSetupTime, final long timeSinceArrival,
				final int priority, final long lateness, final String recipe) {
			super(remainingCycleTime, processingTime, expectedSetupTime, timeSinceArrival, priority, lateness,
					recipe);
			this.id = id;
		}

		public static FlowItemQueuedWithIDSnapshot capture(final AbstractFlowItem item, final AbstractTool tool,
				final long currentTime) {
			return new FlowItemQueuedWithIDSnapshot(item.getId(), calculateRemainingCycleTime(item),
					calculateProcessingTime(item), calculateExpectedSetupTime(tool, item),
					calculateTimeSinceArrival(item, currentTime), calculatePriority(item),
					calculateLateness(item, currentTime), calculateRecipe(item));
		}

		public long getId() {
			return this.id;
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
				return 0L;
			}
			long totalLateness = 0L;
			for (final AbstractFlowItem child : batch.getItems()) {
				totalLateness += calculateLateness(child, currentTime);
			}
			return Math.round(totalLateness / (double) batch.getItems().size());
		}
		return 0L;
	}
}
