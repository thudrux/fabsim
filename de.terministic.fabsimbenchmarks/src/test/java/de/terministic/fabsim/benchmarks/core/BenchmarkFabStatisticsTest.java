package de.terministic.fabsim.benchmarks.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;

class BenchmarkFabStatisticsTest {
	private static final long MINUTE = BenchmarkFab.MINUTE;
	private static final long HOUR = BenchmarkFab.HOUR;

	@Test
	void aggregatesEligibleCompletedLotsAfterWarmup() {
		final FabModel model = new FabModel();
		final AbstractComponent sink = (AbstractComponent) model.getSimComponentFactory().createSink("Sink");
		final Product product = productWithProcessingTime(model, sink, 3L * MINUTE);
		final BenchmarkFabStatistics statistics = new BenchmarkFabStatistics(48L, 24L, 24L * HOUR);

		statistics.processEvent(destructionEvent(model, sink, lot(model, product, 7, 1,
				23L * HOUR, 0L), 23L * HOUR));
		statistics.processEvent(new NonDestructionEvent(model, 25L * HOUR, sink,
				lot(model, product, 8, 1, 25L * HOUR, 0L)));
		statistics.processEvent(destructionEvent(model, sink, new TestFlowItem(model), 25L * HOUR));

		final Lot firstCompletedLot = lot(model, product, 4, 2, 24L * HOUR + 6L * MINUTE,
				24L * HOUR + 1L * MINUTE);
		final Lot secondCompletedLot = lot(model, product, 6, 1, 24L * HOUR + 20L * MINUTE,
				24L * HOUR + 14L * MINUTE);
		statistics.processEvent(destructionEvent(model, sink, firstCompletedLot, 24L * HOUR + 10L * MINUTE));
		statistics.processEvent(destructionEvent(model, sink, secondCompletedLot, 24L * HOUR + 20L * MINUTE));

		Assertions.assertEquals(10.0d, statistics.getCompletedWafersPerDay(), 0.000001d);
		Assertions.assertEquals(0.8d, statistics.getTardinessPerWaferMinutes(), 0.000001d);
		Assertions.assertEquals(2.5d, statistics.getFlowFactor(), 0.000001d);
	}

	@Test
	void returnsZeroStatisticsWhenNoEligibleLotsWereObserved() {
		final BenchmarkFabStatistics statistics = new BenchmarkFabStatistics(48L, 24L, 24L * HOUR);

		Assertions.assertEquals(0.0d, statistics.getCompletedWafersPerDay(), 0.000001d);
		Assertions.assertEquals(0.0d, statistics.getTardinessPerWaferMinutes(), 0.000001d);
		Assertions.assertEquals(0.0d, statistics.getFlowFactor(), 0.000001d);
	}

	private static FlowItemDestructionEvent destructionEvent(final FabModel model,
			final AbstractComponent component, final AbstractFlowItem flowItem, final long eventTime) {
		return new FlowItemDestructionEvent(model, eventTime, component, flowItem);
	}

	private static Product productWithProcessingTime(final FabModel model, final AbstractComponent component,
			final long processingTime) {
		final Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe(
				"Step", component, processingTime, ProcessType.LOT, recipe);
		return model.getSimComponentFactory().createProduct("Product", recipe);
	}

	private static Lot lot(final FabModel model, final Product product, final int lotSize,
			final int priority, final long dueDate, final long creationTime) {
		final Lot lot = new Lot(model, product, lotSize, priority, dueDate);
		lot.setCreationTime(creationTime);
		return lot;
	}

	private static final class NonDestructionEvent extends AbstractSimEvent {
		private NonDestructionEvent(final FabModel model, final long time,
				final AbstractComponent component, final AbstractFlowItem flowItem) {
			super(model, time, component, flowItem);
		}

		@Override
		public int getPriority() {
			return 100;
		}
	}

	private static final class TestFlowItem extends AbstractFlowItem {
		private TestFlowItem(final FabModel model) {
			super(model);
		}

		@Override
		public int getSize() {
			return 99;
		}
	}
}
