package de.terministic.fabsim.benchmarks.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.terministic.fabsim.benchmarks.core.specs.FabSpec;
import de.terministic.fabsim.benchmarks.core.specs.MaintenanceSpec;
import de.terministic.fabsim.benchmarks.core.specs.ProductSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteStepSpec;
import de.terministic.fabsim.benchmarks.core.specs.ToolGroupSpec;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;

class SpecContractTest {

	@Test
	void fabSpecRequiresToolGroupsAndProducts() {
		final FabModel model = new FabModel();

		assertIllegalArgumentMessage("toolGroups must not be empty",
				() -> new FabSpec("Sink", 1, false, Collections.<ToolGroupSpec>emptyList(),
						Arrays.asList(productSpec(model))));
		assertIllegalArgumentMessage("products must not be empty",
				() -> new FabSpec("Sink", 1, false, Arrays.asList(toolGroupSpec(model)),
						Collections.<ProductSpec>emptyList()));
	}

	@Test
	void routeSpecRequiresAtLeastOneStep() {
		assertIllegalArgumentMessage("steps must not be empty",
				() -> new RouteSpec(Collections.<RouteStepSpec>emptyList()));
	}

	@Test
	void routeStepSpecEnforcesBatchConfiguration() {
		final FabModel model = new FabModel();

		assertIllegalArgumentMessage("batchMinimum and batchMaximum are required for BATCH steps",
				() -> new RouteStepSpec("ToolGroup", ProcessType.BATCH, value(model, 1L)));
		assertIllegalArgumentMessage("batchMinimum must be greater than 0",
				() -> new RouteStepSpec("ToolGroup", ProcessType.BATCH, value(model, 1L), 0, 1));
		assertIllegalArgumentMessage("batchMinimum must be smaller or equal to batchMaximum",
				() -> new RouteStepSpec("ToolGroup", ProcessType.BATCH, value(model, 1L), 2, 1));
		assertIllegalArgumentMessage("batchMinimum and batchMaximum are only supported for BATCH steps",
				() -> new RouteStepSpec("ToolGroup", ProcessType.LOT, value(model, 1L), 1, 2));

		final RouteStepSpec batchStep = new RouteStepSpec("ToolGroup", ProcessType.BATCH, value(model, 1L), 2, 3);
		Assertions.assertEquals(Integer.valueOf(2), batchStep.getBatchMinimum());
		Assertions.assertEquals(Integer.valueOf(3), batchStep.getBatchMaximum());
	}

	@Test
	void specListsAreImmutableDefensiveCopies() {
		final FabModel model = new FabModel();
		final List<ToolGroupSpec> toolGroups = new ArrayList<>();
		final List<ProductSpec> products = new ArrayList<>();
		toolGroups.add(toolGroupSpec(model));
		products.add(productSpec(model));

		final FabSpec fabSpec = new FabSpec("Sink", 1, false, toolGroups, products);
		toolGroups.clear();
		products.clear();

		Assertions.assertEquals(1, fabSpec.getToolGroups().size());
		Assertions.assertEquals(1, fabSpec.getProducts().size());
		assertUnsupportedOperation(() -> fabSpec.getToolGroups().add(toolGroupSpec(model)));
		assertUnsupportedOperation(() -> fabSpec.getProducts().add(productSpec(model)));

		final List<RouteStepSpec> routeSteps = new ArrayList<>();
		routeSteps.add(routeStep(model));
		final RouteSpec routeSpec = new RouteSpec(routeSteps);
		routeSteps.clear();

		Assertions.assertEquals(1, routeSpec.getSteps().size());
		assertUnsupportedOperation(() -> routeSpec.getSteps().add(routeStep(model)));
	}

	private static ProductSpec productSpec(final FabModel model) {
		return new ProductSpec("Product", 1, value(model, 1L), 1L, RouteSpec.of(routeStep(model)));
	}

	private static ToolGroupSpec toolGroupSpec(final FabModel model) {
		return new ToolGroupSpec("ToolGroup", 1, ProcessingType.LOT, 0L, 0L,
				new MaintenanceSpec(value(model, 1L), value(model, 0L)));
	}

	private static RouteStepSpec routeStep(final FabModel model) {
		return new RouteStepSpec("ToolGroup", ProcessType.LOT, value(model, 1L));
	}

	private static IValue value(final FabModel model, final long value) {
		return model.getValueObjectFactory().createConstantValueObject(value);
	}

	private static void assertIllegalArgumentMessage(final String expectedMessage,
			final Executable executable) {
		final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);
		Assertions.assertEquals(expectedMessage, exception.getMessage());
	}

	private static void assertUnsupportedOperation(final Executable executable) {
		final UnsupportedOperationException exception = Assertions.assertThrows(
				UnsupportedOperationException.class, executable);
		Assertions.assertNotNull(exception);
	}
}
