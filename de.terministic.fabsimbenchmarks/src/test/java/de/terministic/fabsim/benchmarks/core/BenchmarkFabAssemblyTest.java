package de.terministic.fabsim.benchmarks.core;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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
import de.terministic.fabsim.metamodel.components.LotSource;
import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.dispatchrules.FIFO;

class BenchmarkFabAssemblyTest {

	@Test
	void createsProductLineWithRecipeSourceAndSinkStep() {
		final FabModel model = new FabModel();
		final TestBenchmarkFab benchmarkFab = new TestBenchmarkFab(validSpec(model,
				routeStep(model, "LotToolGroup", ProcessType.LOT)));
		final FabSpec fabSpec = benchmarkFab.createFabSpec(model);
		final Sink sink = (Sink) model.getSimComponentFactory().createSink(fabSpec.getSinkName());
		final Map<String, ToolGroup> toolGroups = benchmarkFab.createToolGroupsFor(model, fabSpec);
		final BenchmarkFabSetupManager setupManager = new BenchmarkFabSetupManager(model, fabSpec, toolGroups);

		final LotSource source = benchmarkFab.createProductLineFor(model, fabSpec.getProducts().get(0), sink,
				toolGroups, toolGroupSpecs(fabSpec), setupManager, fabSpec);
		final Product product = source.getProduct();
		final Recipe recipe = product.getRecipe();

		Assertions.assertEquals("Source_Product", source.getName());
		Assertions.assertEquals(5, source.getLotSize());
		Assertions.assertTrue(source.isAllowSplit());
		Assertions.assertEquals("Product", product.getName());
		Assertions.assertEquals(2, recipe.size());
		Assertions.assertSame(sink, recipe.get(1).getComponent());
	}

	@Test
	void rejectsDuplicateToolGroupNames() {
		final FabModel model = new FabModel();
		final FabSpec fabSpec = new FabSpec("Sink", 5, false,
				Arrays.asList(toolGroupSpec(model, "Duplicate", ProcessingType.LOT),
						toolGroupSpec(model, "Duplicate", ProcessingType.LOT)),
				Arrays.asList(productSpec(model, routeStep(model, "Duplicate", ProcessType.LOT))));
		final TestBenchmarkFab benchmarkFab = new TestBenchmarkFab(fabSpec);

		assertIllegalArgumentMessage("Duplicate toolgroup name: Duplicate",
				() -> benchmarkFab.createToolGroupsFor(model, fabSpec));
	}

	@Test
	void rejectsUnknownToolGroupInRoute() {
		final FabModel model = new FabModel();
		final FabSpec fabSpec = validSpec(model, routeStep(model, "MissingToolGroup", ProcessType.LOT));
		final TestBenchmarkFab benchmarkFab = new TestBenchmarkFab(fabSpec);
		final Map<String, ToolGroup> toolGroups = benchmarkFab.createToolGroupsFor(model, fabSpec);
		final BenchmarkFabSetupManager setupManager = new BenchmarkFabSetupManager(model, fabSpec, toolGroups);

		assertIllegalArgumentMessage("Unknown toolgroup in route step: MissingToolGroup",
				() -> benchmarkFab.createRecipeFor(model, fabSpec.getProducts().get(0),
						toolGroups, toolGroupSpecs(fabSpec), setupManager));
	}

	@Test
	void rejectsBatchRouteStepOnLotToolGroup() {
		final FabModel model = new FabModel();
		final FabSpec fabSpec = validSpec(model, batchRouteStep(model, "LotToolGroup"));
		final TestBenchmarkFab benchmarkFab = new TestBenchmarkFab(fabSpec);
		final Map<String, ToolGroup> toolGroups = benchmarkFab.createToolGroupsFor(model, fabSpec);
		final BenchmarkFabSetupManager setupManager = new BenchmarkFabSetupManager(model, fabSpec, toolGroups);

		assertIllegalArgumentMessage("BATCH step LotToolGroup must use a BATCH toolgroup",
				() -> benchmarkFab.createRecipeFor(model, fabSpec.getProducts().get(0),
						toolGroups, toolGroupSpecs(fabSpec), setupManager));
	}

	@Test
	void rejectsLotRouteStepOnBatchToolGroup() {
		final FabModel model = new FabModel();
		final FabSpec fabSpec = new FabSpec("Sink", 5, false,
				Arrays.asList(toolGroupSpec(model, "BatchToolGroup", ProcessingType.BATCH)),
				Arrays.asList(productSpec(model, routeStep(model, "BatchToolGroup", ProcessType.LOT))));
		final TestBenchmarkFab benchmarkFab = new TestBenchmarkFab(fabSpec);
		final Map<String, ToolGroup> toolGroups = benchmarkFab.createToolGroupsFor(model, fabSpec);
		final BenchmarkFabSetupManager setupManager = new BenchmarkFabSetupManager(model, fabSpec, toolGroups);

		assertIllegalArgumentMessage("Non-BATCH step BatchToolGroup must not use a BATCH toolgroup",
				() -> benchmarkFab.createRecipeFor(model, fabSpec.getProducts().get(0),
						toolGroups, toolGroupSpecs(fabSpec), setupManager));
	}

	@Test
	void createsBatchDetailsOnlyForBatchRouteSteps() {
		final FabModel model = new FabModel();
		final FabSpec fabSpec = new FabSpec("Sink", 5, false,
				Arrays.asList(toolGroupSpec(model, "BatchToolGroup", ProcessingType.BATCH),
						toolGroupSpec(model, "LotToolGroup", ProcessingType.LOT)),
				Arrays.asList(productSpec(model,
						batchRouteStep(model, "BatchToolGroup"),
						routeStep(model, "LotToolGroup", ProcessType.LOT))));
		final TestBenchmarkFab benchmarkFab = new TestBenchmarkFab(fabSpec);
		final Map<String, ToolGroup> toolGroups = benchmarkFab.createToolGroupsFor(model, fabSpec);
		final BenchmarkFabSetupManager setupManager = new BenchmarkFabSetupManager(model, fabSpec, toolGroups);

		final Recipe recipe = benchmarkFab.createRecipeFor(model, fabSpec.getProducts().get(0),
				toolGroups, toolGroupSpecs(fabSpec), setupManager);
		final ProcessStep batchStep = recipe.get(0);
		final ProcessStep lotStep = recipe.get(1);

		Assertions.assertNotNull(batchStep.getBatchDetails());
		Assertions.assertNull(lotStep.getBatchDetails());
	}

	private static FabSpec validSpec(final FabModel model, final RouteStepSpec routeStep) {
		return new FabSpec("Sink", 5, true,
				Arrays.asList(toolGroupSpec(model, "LotToolGroup", ProcessingType.LOT)),
				Arrays.asList(productSpec(model, routeStep)));
	}

	private static ProductSpec productSpec(final FabModel model, final RouteStepSpec... routeSteps) {
		return new ProductSpec("Product", 3, value(model, 1L), 10L, RouteSpec.of(routeSteps));
	}

	private static ToolGroupSpec toolGroupSpec(final FabModel model, final String name,
			final ProcessingType processingType) {
		return new ToolGroupSpec(name, 1, processingType, 0L, 0L,
				new MaintenanceSpec(value(model, 1L), value(model, 0L)));
	}

	private static RouteStepSpec routeStep(final FabModel model, final String toolGroupName,
			final ProcessType processType) {
		return new RouteStepSpec(toolGroupName, processType, value(model, 1L));
	}

	private static RouteStepSpec batchRouteStep(final FabModel model, final String toolGroupName) {
		return new RouteStepSpec(toolGroupName, ProcessType.BATCH, value(model, 1L), 2, 4);
	}

	private static Map<String, ToolGroupSpec> toolGroupSpecs(final FabSpec fabSpec) {
		final LinkedHashMap<String, ToolGroupSpec> toolGroupSpecs = new LinkedHashMap<>();
		for (final ToolGroupSpec toolGroupSpec : fabSpec.getToolGroups()) {
			toolGroupSpecs.put(toolGroupSpec.getName(), toolGroupSpec);
		}
		return toolGroupSpecs;
	}

	private static IValue value(final FabModel model, final long value) {
		return model.getValueObjectFactory().createConstantValueObject(value);
	}

	private static void assertIllegalArgumentMessage(final String expectedMessage,
			final Executable executable) {
		final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);
		Assertions.assertEquals(expectedMessage, exception.getMessage());
	}

	private static final class TestBenchmarkFab extends BenchmarkFab {
		private final FabSpec fabSpec;

		private TestBenchmarkFab(final FabSpec fabSpec) {
			super(new FIFO(), null);
			this.fabSpec = fabSpec;
		}

		private Map<String, ToolGroup> createToolGroupsFor(final FabModel model, final FabSpec fabSpec) {
			return createToolGroups(model, new FIFO(), fabSpec);
		}

		private LotSource createProductLineFor(final FabModel model, final ProductSpec productSpec,
				final Sink sink, final Map<String, ToolGroup> toolGroups,
				final Map<String, ToolGroupSpec> toolGroupSpecs,
				final BenchmarkFabSetupManager setupManager, final FabSpec fabSpec) {
			return createProductLine(model, productSpec, sink, toolGroups, toolGroupSpecs, setupManager, fabSpec);
		}

		private Recipe createRecipeFor(final FabModel model, final ProductSpec productSpec,
				final Map<String, ToolGroup> toolGroups, final Map<String, ToolGroupSpec> toolGroupSpecs,
				final BenchmarkFabSetupManager setupManager) {
			return createRecipe(model, productSpec, toolGroups, toolGroupSpecs, setupManager);
		}

		@Override
		protected String getBenchmarkName() {
			return "TestBenchmarkFab";
		}

		@Override
		protected FabSpec createFabSpec(final FabModel model) {
			return this.fabSpec;
		}
	}
}
