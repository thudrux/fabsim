package de.terministic.fabsim.benchmarks.core.setup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.benchmarks.core.specs.FabSpec;
import de.terministic.fabsim.benchmarks.core.specs.ProductSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteStepSpec;
import de.terministic.fabsim.benchmarks.core.specs.SetupTimeSpec;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

public final class SetupManager {
	private final Map<String, Map<String, SetupState>> setupStates;

	public SetupManager(final FabModel model, final FabSpec fabSpec,
			final Map<String, ToolGroup> toolGroups) {
		final List<RouteStepSetupContext> contexts = getRouteStepSetupContexts(fabSpec);
		this.setupStates = createSetupStates(model, contexts, toolGroups);
		createSetupTransitions(model, groupByToolGroup(contexts), toolGroups);
	}

	public SetupState getTargetSetupState(final RouteStepSpec stepSpec) {
		if (stepSpec.getSetupStateName() == null) {
			return null;
		}
		final Map<String, SetupState> toolGroupSetupStates = this.setupStates.get(stepSpec.getToolGroupName());
		if (toolGroupSetupStates == null) {
			throw new IllegalArgumentException("Unknown setup state " + stepSpec.getSetupStateName()
					+ " for toolgroup " + stepSpec.getToolGroupName());
		}
		final SetupState targetSetupState = toolGroupSetupStates.get(stepSpec.getSetupStateName());
		if (targetSetupState == null) {
			throw new IllegalArgumentException("Unknown setup state " + stepSpec.getSetupStateName()
					+ " for toolgroup " + stepSpec.getToolGroupName());
		}
		return targetSetupState;
	}

	private static Map<String, Map<String, SetupState>> createSetupStates(final FabModel model,
			final List<RouteStepSetupContext> contexts, final Map<String, ToolGroup> toolGroups) {
		final LinkedHashMap<String, Map<String, SetupState>> setupStates = new LinkedHashMap<>();
		for (final RouteStepSetupContext context : contexts) {
			Map<String, SetupState> toolGroupSetupStates = setupStates.get(context.toolGroupName);
			if (toolGroupSetupStates == null) {
				toolGroupSetupStates = new LinkedHashMap<>();
				setupStates.put(context.toolGroupName, toolGroupSetupStates);
			}
			if (!toolGroupSetupStates.containsKey(context.setupStateName)) {
				final ToolGroup toolGroup = toolGroups.get(context.toolGroupName);
				if (toolGroup == null) {
					throw new IllegalArgumentException("Unknown toolgroup in route step: " + context.toolGroupName);
				}
				final SetupState setupState = model.getSimComponentFactory()
						.createSetupStateAndAddToToolGroup(context.setupStateName, toolGroup);
				toolGroupSetupStates.put(context.setupStateName, setupState);
			}
		}
		return setupStates;
	}

	private void createSetupTransitions(final FabModel model,
			final Map<String, List<RouteStepSetupContext>> contextsByToolGroup,
			final Map<String, ToolGroup> toolGroups) {
		for (final Map.Entry<String, List<RouteStepSetupContext>> entry : contextsByToolGroup.entrySet()) {
			final String toolGroupName = entry.getKey();
			final ToolGroup toolGroup = toolGroups.get(toolGroupName);
			final Map<String, SetupState> toolGroupSetupStates = this.setupStates.get(toolGroupName);
			if (toolGroup == null || toolGroupSetupStates == null) {
				continue;
			}
			createSetupTransitions(model, toolGroupName, toolGroup, toolGroupSetupStates, entry.getValue());
		}
	}

	private static void createSetupTransitions(final FabModel model, final String toolGroupName,
			final ToolGroup toolGroup, final Map<String, SetupState> toolGroupSetupStates,
			final List<RouteStepSetupContext> contexts) {
		final LinkedHashMap<SetupTransitionKey, Long> transitionTimes = new LinkedHashMap<>();
		for (final RouteStepSetupContext currentContext : contexts) {
			for (final RouteStepSetupContext targetContext : contexts) {
				if (currentContext.setupStateName.equals(targetContext.setupStateName)) {
					continue;
				}
				putTransitionTime(toolGroupName, transitionTimes, currentContext, targetContext);
			}
		}
		for (final Map.Entry<SetupTransitionKey, Long> transition : transitionTimes.entrySet()) {
			final SetupTransitionKey key = transition.getKey();
			model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(
					toolGroupSetupStates.get(key.currentSetupStateName),
					toolGroupSetupStates.get(key.targetSetupStateName), transition.getValue(), false,
					toolGroup);
		}
	}

	private static void putTransitionTime(final String toolGroupName,
			final Map<SetupTransitionKey, Long> transitionTimes, final RouteStepSetupContext currentContext,
			final RouteStepSetupContext targetContext) {
		final long setupTime = resolveSetupTime(currentContext, targetContext);
		final SetupTransitionKey key = new SetupTransitionKey(currentContext.setupStateName,
				targetContext.setupStateName);
		final Long existingSetupTime = transitionTimes.get(key);
		if (existingSetupTime != null && existingSetupTime != setupTime) {
			throw new IllegalArgumentException("Conflicting setup times for toolgroup " + toolGroupName
					+ " from setup state " + currentContext.setupStateName + " to " + targetContext.setupStateName);
		}
		transitionTimes.put(key, setupTime);
	}

	private static Map<String, List<RouteStepSetupContext>> groupByToolGroup(
			final List<RouteStepSetupContext> contexts) {
		final LinkedHashMap<String, List<RouteStepSetupContext>> contextsByToolGroup = new LinkedHashMap<>();
		for (final RouteStepSetupContext context : contexts) {
			List<RouteStepSetupContext> toolGroupContexts = contextsByToolGroup.get(context.toolGroupName);
			if (toolGroupContexts == null) {
				toolGroupContexts = new ArrayList<>();
				contextsByToolGroup.put(context.toolGroupName, toolGroupContexts);
			}
			toolGroupContexts.add(context);
		}
		return contextsByToolGroup;
	}

	private static List<RouteStepSetupContext> getRouteStepSetupContexts(final FabSpec fabSpec) {
		final ArrayList<RouteStepSetupContext> contexts = new ArrayList<>();
		for (final ProductSpec productSpec : fabSpec.getProducts()) {
			final List<RouteStepSpec> steps = productSpec.getRoute().getSteps();
			for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
				final RouteStepSpec stepSpec = steps.get(stepIndex);
				if (stepSpec.getSetupStateName() != null) {
					contexts.add(new RouteStepSetupContext(productSpec.getName(), stepIndex + 1,
							stepSpec.getToolGroupName(), stepSpec.getSetupStateName(), stepSpec));
				}
			}
		}
		return contexts;
	}

	private static long resolveSetupTime(final RouteStepSetupContext currentContext,
			final RouteStepSetupContext targetContext) {
		SetupTimeResolution bestResolution = null;
		for (final SetupTimeSpec setupTimeSpec : targetContext.stepSpec.getSetupTimeSpecs()) {
			if (!matches(setupTimeSpec, currentContext)) {
				continue;
			}
			final SetupTimeResolution resolution = new SetupTimeResolution(setupTimeSpec.getSetupTime(),
					getSpecificity(setupTimeSpec));
			if (bestResolution == null || resolution.specificity > bestResolution.specificity) {
				bestResolution = resolution;
			} else if (resolution.specificity == bestResolution.specificity
					&& resolution.setupTime != bestResolution.setupTime) {
				throw new IllegalArgumentException("Conflicting setup time specs for target product "
						+ targetContext.productName + " step " + targetContext.routeStepIndex);
			}
		}
		return bestResolution == null ? 0L : bestResolution.setupTime;
	}

	private static boolean matches(final SetupTimeSpec setupTimeSpec,
			final RouteStepSetupContext currentContext) {
		if (setupTimeSpec.getCurrentProductName() != null
				&& !setupTimeSpec.getCurrentProductName().equals(currentContext.productName)) {
			return false;
		}
		if (setupTimeSpec.getCurrentRouteStepIndex() != null
				&& setupTimeSpec.getCurrentRouteStepIndex() != currentContext.routeStepIndex) {
			return false;
		}
		return setupTimeSpec.getCurrentSetupStateName() == null
				|| setupTimeSpec.getCurrentSetupStateName().equals(currentContext.setupStateName);
	}

	private static int getSpecificity(final SetupTimeSpec setupTimeSpec) {
		int specificity = 0;
		if (setupTimeSpec.getCurrentSetupStateName() != null) {
			specificity += 4;
		}
		if (setupTimeSpec.getCurrentProductName() != null) {
			specificity += 2;
		}
		if (setupTimeSpec.getCurrentRouteStepIndex() != null) {
			specificity += 1;
		}
		return specificity;
	}

	private static final class RouteStepSetupContext {
		private final String productName;
		private final int routeStepIndex;
		private final String toolGroupName;
		private final String setupStateName;
		private final RouteStepSpec stepSpec;

		private RouteStepSetupContext(final String productName, final int routeStepIndex, final String toolGroupName,
				final String setupStateName, final RouteStepSpec stepSpec) {
			this.productName = productName;
			this.routeStepIndex = routeStepIndex;
			this.toolGroupName = toolGroupName;
			this.setupStateName = setupStateName;
			this.stepSpec = stepSpec;
		}
	}

	private static final class SetupTimeResolution {
		private final long setupTime;
		private final int specificity;

		private SetupTimeResolution(final long setupTime, final int specificity) {
			this.setupTime = setupTime;
			this.specificity = specificity;
		}
	}

	private static final class SetupTransitionKey {
		private final String currentSetupStateName;
		private final String targetSetupStateName;

		private SetupTransitionKey(final String currentSetupStateName, final String targetSetupStateName) {
			this.currentSetupStateName = currentSetupStateName;
			this.targetSetupStateName = targetSetupStateName;
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof SetupTransitionKey)) {
				return false;
			}
			final SetupTransitionKey otherKey = (SetupTransitionKey) other;
			return this.currentSetupStateName.equals(otherKey.currentSetupStateName)
					&& this.targetSetupStateName.equals(otherKey.targetSetupStateName);
		}

		@Override
		public int hashCode() {
			int result = this.currentSetupStateName.hashCode();
			result = 31 * result + this.targetSetupStateName.hashCode();
			return result;
		}
	}
}
