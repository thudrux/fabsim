package de.terministic.fabsim.metamodel.statistics;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.CreationEvent;
import de.terministic.fabsim.metamodel.components.FlowItemDestructionEvent;

public class TotalWipLogger extends SimEventListener {
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

	private ArrayList<WipLogEntry> log = new ArrayList<>();
	private long currentLots = 0L;
	private long currentWafer = 0L;
	private double avgLotWip = -1.0;
	private double avgWaferWip = -1.0;
	private long maxWip = 0L;

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof CreationEvent) {
			currentLots++;
			currentWafer += event.getFlowItem().getSize();
			WipLogEntry entry = new WipLogEntry();
			entry.setTime(event.getEventTime());
			entry.setLots(currentLots);
			entry.setWafer(currentWafer);
			if (currentLots > maxWip) {
				maxWip = currentLots;
				logger.trace("new maxWip is set to {}", maxWip);
			}
			log.add(entry);
			logger.trace("new item created new wip is {}", currentLots);
		}
		if (event instanceof FlowItemDestructionEvent) {
			currentLots--;
			currentWafer -= event.getFlowItem().getSize();
			WipLogEntry entry = new WipLogEntry();
			entry.setTime(event.getEventTime());
			entry.setLots(currentLots);
			entry.setWafer(currentWafer);
			log.add(entry);
			logger.trace("item left the system new wip is {}", currentLots);
		}
	}

	private void calcAvgWIP(long simStart, long simEnd) {
		long timeTimesLotWip = 0L;
		long timeTimesWaferWip = 0L;
		long intervallEnd = simEnd;
		for (int i = log.size() - 1; i >= 0; i--) {
			WipLogEntry entry = log.get(i);
			logger.trace("adding lot wip*duration to sum {}", (intervallEnd - entry.getTime()) * entry.getLots());
			timeTimesLotWip += (intervallEnd - entry.getTime()) * entry.getLots();
			timeTimesWaferWip += (intervallEnd - entry.getTime()) * entry.getWafer();
			intervallEnd = entry.getTime();
		}
		avgLotWip = timeTimesLotWip / ((simEnd - simStart) * 1.0);
		avgWaferWip = timeTimesWaferWip / ((simEnd - simStart) * 1.0);
	}

	public double getAvgLotWip(long simStart, long simEnd) {
		if (avgLotWip < 0.0) {
			calcAvgWIP(simStart, simEnd);
		}
		return avgLotWip;
	}

	public double getAvgWaferWip(long simStart, long simEnd) {
		if (avgLotWip < 0.0) {
			calcAvgWIP(simStart, simEnd);
		}
		return avgWaferWip;
	}

	public long getMaxWip() {
		return maxWip;
	}

	public void printLog() {
		for (WipLogEntry entry : log) {
			logger.debug("time: {}   || wip: {}", entry.getTime(), entry.getLots());
		}
	}
}
