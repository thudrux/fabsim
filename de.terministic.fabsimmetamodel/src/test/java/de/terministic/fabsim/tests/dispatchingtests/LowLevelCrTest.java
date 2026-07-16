package de.terministic.fabsim.tests.dispatchingtests;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.FabSimulationEngine;
import de.terministic.fabsim.metamodel.TimeStamps;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.dispatchRules.CriticalRatio;

public class LowLevelCrTest {
	private final ArrayList<AbstractFlowItem> items = new ArrayList<>();

	@Test
	public void criticalRatioSelectsLowestRatioItem() {
		final FabModel model = new FabModel();
		final SimulationEngine engine = new FabSimulationEngine();
		final CriticalRatio criticalRatio = new CriticalRatio();
		final Sink sink = (Sink) model.getSimComponentFactory().createSink();

		final Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step", sink, 10L, ProcessType.LOT, recipe);
		final Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		final Lot highRatioItem = new Lot(model, product, 1, 1, 100L);
		final TimeStamps highRatioTime = new TimeStamps(0);
		highRatioTime.setArrivalTime(15L);
		highRatioItem.getTimeStampMap().put(0, highRatioTime);

		final Lot lowRatioItem = new Lot(model, product, 1, 1, 50L);
		final TimeStamps lowRatioTime = new TimeStamps(0);
		lowRatioTime.setArrivalTime(2L);
		lowRatioItem.getTimeStampMap().put(0, lowRatioTime);

		attachSimulationEngine(engine, highRatioItem, lowRatioItem);

		items.add(highRatioItem);
		items.add(lowRatioItem);

		Assertions.assertEquals(lowRatioItem, criticalRatio.getBestItem(items));
	}

	@Test
	public void criticalRatioHandlesBatches() {
		final FabModel model = new FabModel();
		final SimulationEngine engine = new FabSimulationEngine();
		final CriticalRatio criticalRatio = new CriticalRatio();
		final Sink sink = (Sink) model.getSimComponentFactory().createSink();

		final Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step", sink, 10L, ProcessType.LOT, recipe);
		final Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		final Lot batchItem1 = new Lot(model, product, 1, 1, 40L);
		final TimeStamps batchTime1 = new TimeStamps(0);
		batchTime1.setArrivalTime(9L);
		batchItem1.getTimeStampMap().put(0, batchTime1);

		final Lot batchItem2 = new Lot(model, product, 1, 1, 80L);
		final TimeStamps batchTime2 = new TimeStamps(0);
		batchTime2.setArrivalTime(1L);
		batchItem2.getTimeStampMap().put(0, batchTime2);

		final Batch batch = new Batch(model, recipe);
		batch.addItem(batchItem1);
		batch.addItem(batchItem2);

		final Lot competingItem = new Lot(model, product, 1, 1, 60L);
		final TimeStamps competingTime = new TimeStamps(0);
		competingTime.setArrivalTime(15L);
		competingItem.getTimeStampMap().put(0, competingTime);

		attachSimulationEngine(engine, batch, batchItem1, batchItem2, competingItem);

		items.add(competingItem);
		items.add(batch);

		Assertions.assertEquals(batch, criticalRatio.getBestItem(items));
	}

	@Test
	public void criticalRatioUsesDeterministicTieBreakingWhenRatiosMatch() {
		final FabModel model = new FabModel();
		final SimulationEngine engine = new FabSimulationEngine();
		final CriticalRatio criticalRatio = new CriticalRatio();
		final Sink sink = (Sink) model.getSimComponentFactory().createSink();

		final Recipe shortRecipe = model.getSimComponentFactory().createRecipe("ShortRecipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("ShortStep", sink, 10L, ProcessType.LOT,
				shortRecipe);
		final Product shortProduct = model.getSimComponentFactory().createProduct("ShortProduct", shortRecipe);

		final Recipe longerRecipe = model.getSimComponentFactory().createRecipe("LongerRecipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("LongerStep", sink, 12L, ProcessType.LOT,
				longerRecipe);
		final Product longerProduct = model.getSimComponentFactory().createProduct("LongerProduct", longerRecipe);

		final Lot laterArrivalItem = new Lot(model, shortProduct, 1, 1, 50L);
		final TimeStamps laterArrivalTime = new TimeStamps(0);
		laterArrivalTime.setArrivalTime(5L);
		laterArrivalItem.getTimeStampMap().put(0, laterArrivalTime);

		final Lot earlierArrivalItem = new Lot(model, longerProduct, 1, 1, 60L);
		final TimeStamps earlierArrivalTime = new TimeStamps(0);
		earlierArrivalTime.setArrivalTime(2L);
		earlierArrivalItem.getTimeStampMap().put(0, earlierArrivalTime);

		attachSimulationEngine(engine, laterArrivalItem, earlierArrivalItem);

		items.add(laterArrivalItem);
		items.add(earlierArrivalItem);

		Assertions.assertEquals(earlierArrivalItem, criticalRatio.getBestItem(items));
	}

	private void attachSimulationEngine(final SimulationEngine engine, final AbstractFlowItem... flowItems) {
		for (final AbstractFlowItem flowItem : flowItems) {
			flowItem.setupForSimulation(engine);
		}
	}
}
