package de.terministic.fabsim.tests.dispatchingtests;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.TimeStamps;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.BasicFlowItem;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.Sink;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.dispatchRules.SRPT;

public class LowLevelSrptTest {
	SRPT srpt;
	ArrayList<AbstractFlowItem> E = new ArrayList<AbstractFlowItem>();
	FabModel model;

	@Test
	public void SrptTest() {
		E = new ArrayList<AbstractFlowItem>();
		model = new FabModel();
		srpt = new SRPT();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();

		Recipe longRecipe = model.getSimComponentFactory().createRecipe("LongRecipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("LongStep", sink, 10L, ProcessType.LOT,
				longRecipe);
		Product longProduct = model.getSimComponentFactory().createProduct("LongProduct", longRecipe);

		Recipe shortRecipe = model.getSimComponentFactory().createRecipe("ShortRecipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("ShortStep", sink, 3L, ProcessType.LOT,
				shortRecipe);
		Product shortProduct = model.getSimComponentFactory().createProduct("ShortProduct", shortRecipe);

		AbstractFlowItem item1 = new BasicFlowItem(model, longProduct);
		TimeStamps timeStamp1 = new TimeStamps(0);
		timeStamp1.setArrivalTime(15);
		item1.getTimeStampMap().put(0, timeStamp1);

		AbstractFlowItem item2 = new BasicFlowItem(model, shortProduct);
		TimeStamps timeStamp2 = new TimeStamps(0);
		timeStamp2.setArrivalTime(2);
		item2.getTimeStampMap().put(0, timeStamp2);

		AbstractFlowItem item3 = new BasicFlowItem(model, longProduct);
		TimeStamps timeStamp3 = new TimeStamps(0);
		timeStamp3.setArrivalTime(5);
		item3.getTimeStampMap().put(0, timeStamp3);

		E.add(item1);
		E.add(item2);
		E.add(item3);

		Assertions.assertEquals(item2, srpt.getBestItem(E));
	}

	@Test
	public void SrptBatchTest() {
		E = new ArrayList<AbstractFlowItem>();

		model = new FabModel();
		srpt = new SRPT();
		Sink sink = (Sink) model.getSimComponentFactory().createSink();

		Recipe longRecipe = model.getSimComponentFactory().createRecipe("LongRecipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("LongStep", sink, 10L, ProcessType.LOT,
				longRecipe);
		Product longProduct = model.getSimComponentFactory().createProduct("LongProduct", longRecipe);

		Recipe shortRecipe = model.getSimComponentFactory().createRecipe("ShortRecipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("ShortStep", sink, 3L, ProcessType.LOT,
				shortRecipe);
		Product shortProduct = model.getSimComponentFactory().createProduct("ShortProduct", shortRecipe);

		Batch batch = new Batch(model, longRecipe);

		Lot batchItem1 = new Lot(model, longProduct, 1, 1, Long.MAX_VALUE);
		TimeStamps batchTime1 = new TimeStamps(0);
		batchTime1.setArrivalTime(9);
		batchItem1.getTimeStampMap().put(0, batchTime1);

		Lot batchItem2 = new Lot(model, shortProduct, 1, 1, Long.MAX_VALUE);
		TimeStamps batchTime2 = new TimeStamps(0);
		batchTime2.setArrivalTime(1);
		batchItem2.getTimeStampMap().put(0, batchTime2);

		batch.addItem(batchItem1);
		batch.addItem(batchItem2);

		AbstractFlowItem item1 = new BasicFlowItem(model, shortProduct);
		TimeStamps timeStamp1 = new TimeStamps(0);
		timeStamp1.setArrivalTime(15);
		item1.getTimeStampMap().put(0, timeStamp1);

		AbstractFlowItem item2 = new BasicFlowItem(model, longProduct);
		TimeStamps timeStamp2 = new TimeStamps(0);
		timeStamp2.setArrivalTime(2);
		item2.getTimeStampMap().put(0, timeStamp2);

		E.add(item1);
		E.add(batch);
		E.add(item2);

		Assertions.assertEquals(batch, srpt.getBestItem(E));
	}

}
