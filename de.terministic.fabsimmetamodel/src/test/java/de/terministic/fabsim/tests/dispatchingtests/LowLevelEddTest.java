package de.terministic.fabsim.tests.dispatchingtests;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.TimeStamps;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.dispatchRules.EDD;

public class LowLevelEddTest {
	EDD edd;
	ArrayList<AbstractFlowItem> E = new ArrayList<AbstractFlowItem>();
	FabModel model;
	Recipe recipe = null;

	@Test
	public void EddTest() {
		E = new ArrayList<AbstractFlowItem>();
		model = new FabModel();
		edd = new EDD();
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		Lot item1 = new Lot(model, product, 1, 1, 15);
		TimeStamps timeStamp1 = new TimeStamps(0);
		timeStamp1.setArrivalTime(15);
		item1.getTimeStampMap().put(0, timeStamp1);

		Lot item2 = new Lot(model, product, 1, 1, 2);
		TimeStamps timeStamp2 = new TimeStamps(0);
		timeStamp2.setArrivalTime(2);
		item2.getTimeStampMap().put(0, timeStamp2);

		Lot item3 = new Lot(model, product, 1, 1, 5);
		TimeStamps timeStamp3 = new TimeStamps(0);
		timeStamp3.setArrivalTime(5);
		item3.getTimeStampMap().put(0, timeStamp3);

		E.add(item1);
		E.add(item2);
		E.add(item3);

		Assertions.assertEquals(item2, edd.getBestItem(E));
	}

	@Test
	public void EddBatchTest() {
		E = new ArrayList<AbstractFlowItem>();

		model = new FabModel();
		edd = new EDD();
		Recipe batchRecipe = model.getSimComponentFactory().createRecipe("BatchRecipe");
		Product product = model.getSimComponentFactory().createProduct("BatchProduct", batchRecipe);

		Lot batchItem1 = new Lot(model, product, 1, 1, 20);
		TimeStamps batchTime1 = new TimeStamps(0);
		batchTime1.setArrivalTime(9);
		batchItem1.getTimeStampMap().put(0, batchTime1);

		Lot batchItem2 = new Lot(model, product, 1, 1, 4);
		TimeStamps batchTime2 = new TimeStamps(0);
		batchTime2.setArrivalTime(1);
		batchItem2.getTimeStampMap().put(0, batchTime2);

		Batch batch = new Batch(model, batchRecipe);
		batch.addItem(batchItem1);
		batch.addItem(batchItem2);

		Lot item1 = new Lot(model, product, 1, 1, 6);
		TimeStamps timeStamp1 = new TimeStamps(0);
		timeStamp1.setArrivalTime(15);
		item1.getTimeStampMap().put(0, timeStamp1);

		Lot item2 = new Lot(model, product, 1, 1, 8);
		TimeStamps timeStamp2 = new TimeStamps(0);
		timeStamp2.setArrivalTime(2);
		item2.getTimeStampMap().put(0, timeStamp2);

		E.add(item1);
		E.add(batch);
		E.add(item2);

		Assertions.assertEquals(batch, edd.getBestItem(E));
	}

}
