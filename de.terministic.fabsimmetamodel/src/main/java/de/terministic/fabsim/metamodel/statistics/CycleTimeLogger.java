package de.terministic.fabsim.metamodel.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.CreationEvent;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.metamodel.components.Product;

public class CycleTimeLogger extends SimEventListener {
	private Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getSimpleName());
	private final String CREATIONTIMEMARKER = "CycleTimeLogger_CREATION_TIME";
	private final String DESTRUCTIONTIMEMARKER = "CycleTimeLogger_DESTRUCTION_TIME";

	private HashMap<Product, ArrayList<Long>> ctLists = new LinkedHashMap<Product, ArrayList<Long>>();
	private long rptSum = 0L;

	public List<Long> getAllCycleTimes() {
		ArrayList<Long> result = new ArrayList<>();
		for (ArrayList<Long> list : ctLists.values()) {
			result.addAll(list);
		}
		return result;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof CreationEvent) {
			event.getFlowItem().getStatistics().put(CREATIONTIMEMARKER, event.getEventTime());
		}
		if (event instanceof FlowItemDestructionEvent) {
			event.getFlowItem().getStatistics().put(DESTRUCTIONTIMEMARKER, event.getEventTime());
			// logger.debug("Event: {}\n", event);
			// logger.debug("FlowItemStats: {}\n",
			// event.getFlowItem().getStatistics());
			if (!ctLists.containsKey(event.getFlowItem().getProduct())) {
				ctLists.put(event.getFlowItem().getProduct(), new ArrayList<Long>());
			}
			ctLists.get(event.getFlowItem().getProduct())
					.add(event.getEventTime() - event.getFlowItem().getStatistics().get(CREATIONTIMEMARKER));
			setRptSum(getRptSum() + event.getFlowItem().getRPT());
		}
	}

	public long getRptSum() {
		return rptSum;
	}

	public void setRptSum(long rptSum) {
		this.rptSum = rptSum;
	}

}
