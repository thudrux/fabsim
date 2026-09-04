package de.terministic.fabsim.benchmarks.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.terministic.fabsim.benchmarks.core.specs.FabSpec;
import de.terministic.fabsim.benchmarks.core.specs.MaintenanceSpec;
import de.terministic.fabsim.benchmarks.core.specs.ProductSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteStepSpec;
import de.terministic.fabsim.benchmarks.core.specs.SetupTimeSpec;
import de.terministic.fabsim.benchmarks.core.specs.ToolGroupSpec;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

class BenchmarkFabSetupManagerTest {

	@Test
	void createsTargetSetupStatesAndResolvesTransitionTimesBySpecificity() {
		final FabModel model = new FabModel();
		final Map<String, ToolGroup> toolGroups = toolGroups(model, "ToolGroup");
		final RouteStepSpec stepA = setupStep(model, "ToolGroup", "A", Collections.<SetupTimeSpec>emptyList());
		final RouteStepSpec stepB = setupStep(model, "ToolGroup", "B", Arrays.asList(
				SetupTimeSpec.fromAnyCurrentState(7L),
				SetupTimeSpec.from("Product", 1, "A", 11L)));
		final BenchmarkFabSetupManager setupManager = new BenchmarkFabSetupManager(model,
				fabSpec(model, stepA, stepB), toolGroups);

		final SetupState stateA = setupManager.getTargetSetupState(stepA);
		final SetupState stateB = setupManager.getTargetSetupState(stepB);

		Assertions.assertEquals("A", stateA.getSetupName());
		Assertions.assertEquals("B", stateB.getSetupName());
		Assertions.assertEquals(11L, transitionTime(toolGroups.get("ToolGroup"), "A", "B"));
		Assertions.assertEquals(0L, transitionTime(toolGroups.get("ToolGroup"), "B", "A"));
	}

	@Test
	void rejectsConflictingSetupTimeSpecsWithEqualSpecificity() {
		final FabModel model = new FabModel();
		final Map<String, ToolGroup> toolGroups = toolGroups(model, "ToolGroup");
		final RouteStepSpec stepA = setupStep(model, "ToolGroup", "A", Collections.<SetupTimeSpec>emptyList());
		final RouteStepSpec stepB = setupStep(model, "ToolGroup", "B", Arrays.asList(
				SetupTimeSpec.fromAnyCurrentState(7L),
				SetupTimeSpec.fromAnyCurrentState(9L)));

		assertIllegalArgumentMessage("Conflicting setup time specs for target product Product step 2",
				() -> new BenchmarkFabSetupManager(model, fabSpec(model, stepA, stepB), toolGroups));
	}

	@Test
	void rejectsSetupStateOnUnknownToolGroup() {
		final FabModel model = new FabModel();
		final Map<String, ToolGroup> toolGroups = toolGroups(model, "KnownToolGroup");
		final RouteStepSpec step = setupStep(model, "MissingToolGroup", "A",
				Collections.<SetupTimeSpec>emptyList());

		assertIllegalArgumentMessage("Unknown toolgroup in route step: MissingToolGroup",
				() -> new BenchmarkFabSetupManager(model, fabSpec(model, step), toolGroups));
	}

	private static long transitionTime(final ToolGroup toolGroup, final String currentStateName,
			final String targetStateName) {
		final SetupState currentState = setupState(toolGroup, currentStateName);
		final SetupState targetState = setupState(toolGroup, targetStateName);
		return toolGroup.getSetupTransitions().get(currentState).get(targetState);
	}

	private static SetupState setupState(final ToolGroup toolGroup, final String setupStateName) {
		for (final SetupState setupState : toolGroup.getSetupStates()) {
			if (setupStateName.equals(setupState.getSetupName())) {
				return setupState;
			}
		}
		throw new AssertionError("Missing setup state " + setupStateName);
	}

	private static Map<String, ToolGroup> toolGroups(final FabModel model, final String toolGroupName) {
		final ToolGroup toolGroup = (ToolGroup) model.getSimComponentFactory()
				.createToolGroup(toolGroupName, 1, ProcessingType.LOT);
		final LinkedHashMap<String, ToolGroup> toolGroups = new LinkedHashMap<>();
		toolGroups.put(toolGroupName, toolGroup);
		return toolGroups;
	}

	private static FabSpec fabSpec(final FabModel model, final RouteStepSpec... steps) {
		return new FabSpec("Sink", 1, false,
				Arrays.asList(toolGroupSpec(model, "ToolGroup")),
				Arrays.asList(new ProductSpec("Product", 1, value(model, 1L), 1L, RouteSpec.of(steps))));
	}

	private static ToolGroupSpec toolGroupSpec(final FabModel model, final String name) {
		return new ToolGroupSpec(name, 1, ProcessingType.LOT, 0L, 0L,
				new MaintenanceSpec(value(model, 1L), value(model, 0L)));
	}

	private static RouteStepSpec setupStep(final FabModel model, final String toolGroupName,
			final String setupStateName, final List<SetupTimeSpec> setupTimeSpecs) {
		return new RouteStepSpec(toolGroupName, ProcessType.LOT, value(model, 1L), setupStateName, setupTimeSpecs);
	}

	private static de.terministic.fabsim.core.duration.IValue value(final FabModel model, final long value) {
		return model.getValueObjectFactory().createConstantValueObject(value);
	}

	private static void assertIllegalArgumentMessage(final String expectedMessage,
			final Executable executable) {
		final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);
		Assertions.assertEquals(expectedMessage, exception.getMessage());
	}
}
