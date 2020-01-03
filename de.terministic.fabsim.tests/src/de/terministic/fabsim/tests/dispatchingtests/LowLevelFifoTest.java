package de.terministic.fabsim.tests.dispatchingtests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import de.terministic.fabsim.components.BasicFlowItem;
import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.EventListManager;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.TimeStamps;
import de.terministic.fabsim.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.dispatchRules.FIFO;
import de.terministic.fabsim.statistics.FinishedFlowItemCounter;

import java.util.ArrayList;




public class LowLevelFifoTest {
	FIFO fifo;
	ArrayList<AbstractFlowItem> E = new ArrayList<AbstractFlowItem>();
	FabModel model;
	Recipe recipe = null;
	
	@Test
	public void FifoTest() {
		
		model = new FabModel();
		fifo = new FIFO();
		Product product=model.getSimComponentFactory().createProduct("Product", recipe);

		AbstractFlowItem item1 = new BasicFlowItem(model,product);
		TimeStamps timeStamp1 = new TimeStamps(0);
		timeStamp1.setArrivalTime(15);
		item1.getTimeStampMap().put(0, timeStamp1);
		
		AbstractFlowItem item2 = new BasicFlowItem(model,product);
		TimeStamps timeStamp2 = new TimeStamps(0);
		timeStamp2.setArrivalTime(2);
		item2.getTimeStampMap().put(0, timeStamp2);
		
		AbstractFlowItem item3 = new BasicFlowItem(model,product);
		TimeStamps timeStamp3 = new TimeStamps(0);
		timeStamp3.setArrivalTime(5);
		item3.getTimeStampMap().put(0, timeStamp3);

		E.add(item1);
		E.add(item2);
		E.add(item3);
		
		fifo.getBestItem(E);
		assertEquals(item2, fifo.getBestItem(E));
	}

}
