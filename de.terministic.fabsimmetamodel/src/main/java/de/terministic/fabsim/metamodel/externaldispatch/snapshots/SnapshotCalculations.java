package de.terministic.fabsim.metamodel.externaldispatch.snapshots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionRequest;

final class SnapshotCalculations {

	private SnapshotCalculations() {
	}

	static long calculateProcessingTime(final AbstractFlowItem item) {
		if (item == null || item.getRecipe() == null) {
			return 0L;
		}
		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return 0L;
		}
		return Math.max(0L, item.getRecipe().get(currentStepNumber).getAvgDuration());
	}

	static long calculateRemainingCycleTime(final AbstractFlowItem item,
			final double projectedCycleTimeFactor) {
		if (item == null || item.getRecipe() == null) {
			return 0L;
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return 0L;
			}
			long totalRemainingCycleTime = 0L;
			for (final AbstractFlowItem child : batch.getItems()) {
				totalRemainingCycleTime += calculateRemainingCycleTime(child, projectedCycleTimeFactor);
			}
			return Math.round(totalRemainingCycleTime / (double) batch.getItems().size());
		}
		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return 0L;
		}
		long remainingProcessTime = 0L;
		for (int i = currentStepNumber; i < item.getRecipe().size(); i++) {
			remainingProcessTime += calculateStepCycleTime(item.getRecipe().get(i));
		}
		return Math.round(remainingProcessTime * projectedCycleTimeFactor);
	}

	static long calculateRemainingCycleTime(final AbstractFlowItem item, final long processingTimeLeft,
			final double projectedCycleTimeFactor) {
		if (item == null || item.getRecipe() == null) {
			return Math.max(0L, processingTimeLeft);
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return Math.max(0L, processingTimeLeft);
			}
			long totalRemainingCycleTime = 0L;
			for (final AbstractFlowItem child : batch.getItems()) {
				totalRemainingCycleTime += calculateRemainingCycleTime(child, processingTimeLeft,
						projectedCycleTimeFactor);
			}
			return Math.round(totalRemainingCycleTime / (double) batch.getItems().size());
		}
		final int currentStepNumber = item.getCurrentStepNumber();
		if (currentStepNumber < 0 || currentStepNumber >= item.getRecipe().size()) {
			return Math.max(0L, processingTimeLeft);
		}
		long futureProcessTime = 0L;
		for (int i = currentStepNumber + 1; i < item.getRecipe().size(); i++) {
			futureProcessTime += calculateStepCycleTime(item.getRecipe().get(i));
		}
		return Math.round((processingTimeLeft + futureProcessTime) * projectedCycleTimeFactor);
	}

	static long calculateWaferLevelProjectedTardiness(final AbstractFlowItem item, final long currentTime,
			final double projectedCycleTimeFactor) {
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
				total += calculateWaferLevelProjectedTardiness(lot, currentTime, projectedCycleTimeFactor);
			}
			return total;
		}
		return item.getSize() * (calculateLateness(item, currentTime)
				+ calculateRemainingCycleTime(item, projectedCycleTimeFactor));
	}

	static long calculateWaferLevelWorkInProgress(final AbstractFlowItem item) {
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

	static long calculateExpectedSetupTime(final AbstractTool tool, final AbstractFlowItem item) {
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

	static long calculateExpectedSetupTime(final ToolGroup toolGroup, final AbstractFlowItem item) {
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

	static String calculateRecipe(final AbstractFlowItem item) {
		if (item == null || item.getRecipe() == null || item.getRecipe().getName() == null) {
			return "";
		}
		return item.getRecipe().getName();
	}

	static long calculateTimeSinceArrival(final AbstractFlowItem item, final long currentTime) {
		if (item == null) {
			return 0L;
		}
		if (item instanceof Batch) {
			final Batch batch = (Batch) item;
			if (batch.getItems().isEmpty()) {
				return calculateSingleItemTimeSinceArrival(item, currentTime);
			}
			long totalTimeSinceArrival = 0L;
			for (final AbstractFlowItem lot : batch.getItems()) {
				totalTimeSinceArrival += calculateSingleItemTimeSinceArrival(lot, currentTime);
			}
			return Math.round(totalTimeSinceArrival / (double) batch.getItems().size());
		}
		return calculateSingleItemTimeSinceArrival(item, currentTime);
	}

	private static long calculateSingleItemTimeSinceArrival(final AbstractFlowItem item, final long currentTime) {
		if (item == null) {
			return 0L;
		}
		return Math.max(0L, currentTime - item.getTimeStamps(item.getCurrentStepNumber()).getArrivalTime());
	}

	static int calculatePriority(final AbstractFlowItem item) {
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

	static long calculateLateness(final AbstractFlowItem item, final long currentTime) {
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

	private static long calculateStepCycleTime(final ProcessStep step) {
		return Math.max(0L, step.getLoadTime()) + Math.max(0L, step.getAvgDuration())
				+ Math.max(0L, step.getUnloadTime());
	}

	static void validateProjectedCycleTimeFactor(final double projectedCycleTimeFactor) {
		DispatchDecisionRequest.validateProjectedCycleTimeFactor(projectedCycleTimeFactor);
	}
}
