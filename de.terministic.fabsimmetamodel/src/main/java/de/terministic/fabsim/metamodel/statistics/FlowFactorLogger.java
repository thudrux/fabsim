package de.terministic.fabsim.metamodel.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class FlowFactorLogger extends SimEventListener {
	private Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getSimpleName());
	private long counter = 0L;
	private boolean warmup = true;

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

	public double getFfSum() {
		return ffSum;
	}

	public void setFfSum(double ffSum) {
		this.ffSum = ffSum;
	}

	private double ffSum = 0L;

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof FlowItemDestructionEvent) {
			counter++;
			double ct = ((FlowItemDestructionEvent) event).getEventTime() - ((AbstractFlowItem) event.getFlowItem()).getCreationTime();
			double rpt = ((AbstractFlowItem) event.getFlowItem()).getRPT();
			ffSum += ct / rpt;
		}
	}

	public double getFF() {
		return ffSum / (1.0 * counter);
	}
}
