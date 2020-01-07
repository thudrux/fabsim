package de.terministic.fabsim.tests.dispatchingtests;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.BasicFlowItem;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.TimeStamps;
import de.terministic.fabsim.dispatchRules.FIFO;

public class LowLevelFifoTest {
	FIFO fifo;
	ArrayList<AbstractFlowItem> E = new ArrayList<AbstractFlowItem>();
	FabModel model;
	Recipe recipe = null;

	@Test
	public void FifoTest() {

		model = new FabModel();
		fifo = new FIFO();
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		AbstractFlowItem item1 = new BasicFlowItem(model, product);
		TimeStamps timeStamp1 = new TimeStamps(0);
		timeStamp1.setArrivalTime(15);
		item1.getTimeStampMap().put(0, timeStamp1);

		AbstractFlowItem item2 = new BasicFlowItem(model, product);
		TimeStamps timeStamp2 = new TimeStamps(0);
		timeStamp2.setArrivalTime(2);
		item2.getTimeStampMap().put(0, timeStamp2);

		AbstractFlowItem item3 = new BasicFlowItem(model, product);
		TimeStamps timeStamp3 = new TimeStamps(0);
		timeStamp3.setArrivalTime(5);
		item3.getTimeStampMap().put(0, timeStamp3);

		E.add(item1);
		E.add(item2);
		E.add(item3);

		fifo.getBestItem(E);
		Assertions.assertEquals(item2, fifo.getBestItem(E));
	}

}
