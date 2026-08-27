package de.terministic.fabsim.benchmarks.implementations;

import java.util.Arrays;
import java.util.List;

import de.terministic.fabsim.benchmarks.core.BenchmarkFab;
import de.terministic.fabsim.benchmarks.core.specs.BreakdownSpec;
import de.terministic.fabsim.benchmarks.core.specs.FabSpec;
import de.terministic.fabsim.benchmarks.core.specs.MaintenanceSpec;
import de.terministic.fabsim.benchmarks.core.specs.ProductSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteSpec;
import de.terministic.fabsim.benchmarks.core.specs.RouteStepSpec;
import de.terministic.fabsim.benchmarks.core.specs.SetupTimeSpec;
import de.terministic.fabsim.benchmarks.core.specs.ToolGroupSpec;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.externaldispatch.LocalLogWriter;

public class MiniFab extends BenchmarkFab {
	public MiniFab(final DispatchProvider provider) {
		super(provider);
	}

	public MiniFab(final AbstractDispatchRule dispatchRule,
			final LocalLogWriter logWriter) {
		super(dispatchRule, logWriter);
	}

	@Override
	protected String getBenchmarkName() {
		return "MiniFab";
	}

	@Override
	protected double getLeadTimeFactor() {
		return 2.5;
	}

	@Override
	protected FabSpec createFabSpec(final FabModel model) {
		final double leadTimeFactor = getLeadTimeFactor();
		return new FabSpec(STEP_SINK, LOT_SIZE, false,
				Arrays.asList(
						new ToolGroupSpec("Station1", 2, ProcessingType.BATCH,
								20L * MINUTE, 40L * MINUTE,
								new MaintenanceSpec(1440L * MINUTE,
										model.getValueObjectFactory().createConstantValueObject(
												75L * MINUTE))),
						new ToolGroupSpec("Station2", 2, ProcessingType.LOT,
								15L * MINUTE, 15L * MINUTE,
								new MaintenanceSpec(720L * MINUTE,
										model.getValueObjectFactory().createConstantValueObject(
												120L * MINUTE)),
								new BreakdownSpec(
										model.getValueObjectFactory().createUniformValueObject(
												1440L * MINUTE, 4560L * MINUTE),
										model.getValueObjectFactory().createUniformValueObject(
												360L * MINUTE, 480L * MINUTE))),
						new ToolGroupSpec("Station3", 1, ProcessingType.LOT,
								10L * MINUTE, 10L * MINUTE,
								new MaintenanceSpec(720L * MINUTE,
										model.getValueObjectFactory().createConstantValueObject(
												30L * MINUTE)))),
				Arrays.asList(
						new ProductSpec("Pa", 1,
								model.getValueObjectFactory().createExponentialValueObject(225L * MINUTE),
								Math.round((225L * MINUTE + 18L * MINUTE + 70L * MINUTE + 34L * MINUTE
										+ 255L * MINUTE + 16L * MINUTE) * leadTimeFactor),
								RouteSpec.of(
										new RouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 225L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new RouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 18L * MINUTE)),
										station3Step(model, "Pa", 3, 70L * MINUTE),
										new RouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 34L * MINUTE)),
										new RouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 255L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										station3Step(model, "Pa", 6, 16L * MINUTE))),
						new ProductSpec("Pb", 1,
								model.getValueObjectFactory().createExponentialValueObject(381L * MINUTE),
								Math.round((225L * MINUTE + 30L * MINUTE + 55L * MINUTE + 50L * MINUTE
										+ 255L * MINUTE + 10L * MINUTE) * leadTimeFactor),
								RouteSpec.of(
										new RouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 225L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new RouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 30L * MINUTE)),
										station3Step(model, "Pb", 3, 55L * MINUTE),
										new RouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 50L * MINUTE)),
										new RouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 255L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										station3Step(model, "Pb", 6, 10L * MINUTE))),
						new ProductSpec("TW", 1,
								model.getValueObjectFactory().createExponentialValueObject(4567L * MINUTE),
								Math.round((225L * MINUTE + 42L * MINUTE + 40L * MINUTE + 66L * MINUTE
										+ 255L * MINUTE + 4L * MINUTE) * leadTimeFactor),
								RouteSpec.of(
										new RouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 225L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new RouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 42L * MINUTE)),
										station3Step(model, "TW", 3, 40L * MINUTE),
										new RouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 66L * MINUTE)),
										new RouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 255L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										station3Step(model, "TW", 6, 4L * MINUTE)))));
	}

	private RouteStepSpec station3Step(final FabModel model, final String productName,
			final int routeStepIndex, final long meanProcessingTime) {
		return new RouteStepSpec("Station3", ProcessType.LOT, processingTime(model, meanProcessingTime),
				station3SetupState(productName, routeStepIndex), station3SetupTimes(productName, routeStepIndex));
	}

	private List<SetupTimeSpec> station3SetupTimes(final String productName, final int routeStepIndex) {
		final int otherRouteStepIndex = routeStepIndex == 3 ? 6 : 3;
		final String firstOtherProductName = productName.equals("Pa") ? "Pb" : "Pa";
		final String secondOtherProductName = productName.equals("TW") ? "Pb" : "TW";
		return Arrays.asList(
				SetupTimeSpec.from(productName, otherRouteStepIndex,
						station3SetupState(productName, otherRouteStepIndex), 10L * MINUTE),
				SetupTimeSpec.from(firstOtherProductName, routeStepIndex,
						station3SetupState(firstOtherProductName, routeStepIndex), 5L * MINUTE),
				SetupTimeSpec.from(secondOtherProductName, routeStepIndex,
						station3SetupState(secondOtherProductName, routeStepIndex), 5L * MINUTE),
				SetupTimeSpec.from(firstOtherProductName, otherRouteStepIndex,
						station3SetupState(firstOtherProductName, otherRouteStepIndex), 12L * MINUTE),
				SetupTimeSpec.from(secondOtherProductName, otherRouteStepIndex,
						station3SetupState(secondOtherProductName, otherRouteStepIndex), 12L * MINUTE));
	}

	private String station3SetupState(final String productName, final int routeStepIndex) {
		return productName + "_S" + routeStepIndex;
	}

	private IValue processingTime(final FabModel model, final long meanProcessingTime) {
		final long variation = Math.round(meanProcessingTime * 0.15);
		return model.getValueObjectFactory().createUniformValueObject(
				meanProcessingTime - variation, meanProcessingTime + variation);
	}
}
