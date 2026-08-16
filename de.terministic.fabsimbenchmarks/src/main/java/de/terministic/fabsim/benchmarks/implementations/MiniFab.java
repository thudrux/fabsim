package de.terministic.fabsim.benchmarks.implementations;

import java.util.Arrays;

import de.terministic.fabsim.benchmarks.core.BenchmarkFab;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkBreakdownSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkFabSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkMaintenanceSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkProductSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkRouteSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkRouteStepSpec;
import de.terministic.fabsim.benchmarks.core.specs.BenchmarkToolGroupSpec;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.logging.LocalLogWriter;

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
	protected double getFlowFactor() {
		return 2.5;
	}

	@Override
	protected BenchmarkFabSpec createFabSpec(final FabModel model) {
		final double flowFactor = getFlowFactor();
		return new BenchmarkFabSpec(STEP_SINK, LOT_SIZE, false,
				Arrays.asList(
						new BenchmarkToolGroupSpec("Station1", 2, ProcessingType.BATCH,
								20L * MINUTE, 40L * MINUTE,
								new BenchmarkMaintenanceSpec(1440L * MINUTE,
										model.getValueObjectFactory().createConstantValueObject(
												75L * MINUTE))),
						new BenchmarkToolGroupSpec("Station2", 2, ProcessingType.LOT,
								15L * MINUTE, 15L * MINUTE,
								new BenchmarkMaintenanceSpec(720L * MINUTE,
										model.getValueObjectFactory().createConstantValueObject(
												120L * MINUTE)),
								new BenchmarkBreakdownSpec(
										model.getValueObjectFactory().createUniformValueObject(
												1440L * MINUTE, 4560L * MINUTE),
										model.getValueObjectFactory().createUniformValueObject(
												360L * MINUTE, 480L * MINUTE))),
						new BenchmarkToolGroupSpec("Station3", 1, ProcessingType.LOT,
								10L * MINUTE, 10L * MINUTE,
								new BenchmarkMaintenanceSpec(720L * MINUTE,
										model.getValueObjectFactory().createConstantValueObject(
												30L * MINUTE)))),
				Arrays.asList(
						new BenchmarkProductSpec("Pa", 1,
								model.getValueObjectFactory().createExponentialValueObject(225L * MINUTE),
								Math.round((225L * MINUTE + 18L * MINUTE + 70L * MINUTE + 34L * MINUTE
										+ 255L * MINUTE + 16L * MINUTE) * flowFactor),
								BenchmarkRouteSpec.of(
										new BenchmarkRouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 225L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new BenchmarkRouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 18L * MINUTE)),
										new BenchmarkRouteStepSpec("Station3", ProcessType.LOT,
												processingTime(model, 70L * MINUTE)),
										new BenchmarkRouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 34L * MINUTE)),
										new BenchmarkRouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 255L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new BenchmarkRouteStepSpec("Station3", ProcessType.LOT,
												processingTime(model, 16L * MINUTE)))),
						new BenchmarkProductSpec("Pb", 1,
								model.getValueObjectFactory().createExponentialValueObject(381L * MINUTE),
								Math.round((225L * MINUTE + 30L * MINUTE + 55L * MINUTE + 50L * MINUTE
										+ 255L * MINUTE + 10L * MINUTE) * flowFactor),
								BenchmarkRouteSpec.of(
										new BenchmarkRouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 225L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new BenchmarkRouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 30L * MINUTE)),
										new BenchmarkRouteStepSpec("Station3", ProcessType.LOT,
												processingTime(model, 55L * MINUTE)),
										new BenchmarkRouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 50L * MINUTE)),
										new BenchmarkRouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 255L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new BenchmarkRouteStepSpec("Station3", ProcessType.LOT,
												processingTime(model, 10L * MINUTE)))),
						new BenchmarkProductSpec("TW", 1,
								model.getValueObjectFactory().createExponentialValueObject(4567L * MINUTE),
								Math.round((225L * MINUTE + 42L * MINUTE + 40L * MINUTE + 66L * MINUTE
										+ 255L * MINUTE + 4L * MINUTE) * flowFactor),
								BenchmarkRouteSpec.of(
										new BenchmarkRouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 225L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new BenchmarkRouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 42L * MINUTE)),
										new BenchmarkRouteStepSpec("Station3", ProcessType.LOT,
												processingTime(model, 40L * MINUTE)),
										new BenchmarkRouteStepSpec("Station2", ProcessType.LOT,
												processingTime(model, 66L * MINUTE)),
										new BenchmarkRouteStepSpec("Station1", ProcessType.BATCH,
												processingTime(model, 255L * MINUTE),
												LOT_SIZE * 3, LOT_SIZE * 3),
										new BenchmarkRouteStepSpec("Station3", ProcessType.LOT,
												processingTime(model, 4L * MINUTE))))));
	}

	private IValue processingTime(final FabModel model, final long meanProcessingTime) {
		final long variation = Math.round(meanProcessingTime * 0.15);
		return model.getValueObjectFactory().createUniformValueObject(
				meanProcessingTime - variation, meanProcessingTime + variation);
	}
}
