package de.terministic.fabsim.metamodel;

import de.terministic.fabsim.core.DummyEvent;
import de.terministic.fabsim.core.EndOfWarmupEvent;
import de.terministic.fabsim.core.IEventListManager;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.SimulatorEngineException;
import de.terministic.fabsim.core.eventlist.PriorityQueueEventListManager;
import de.terministic.fabsim.metamodel.statistics.FinishedFlowItemCounter;
import de.terministic.fabsim.metamodel.statistics.FlowItemCounter;

public class FabSimulationEngine extends SimulationEngine {
	
	public FabSimulationEngine() {
		super(new FabSimEventFactory());
	}

	public FabSimulationEngine(IEventListManager eventList) {
		super(eventList, new FabSimEventFactory());
		

	}

	public void runSimulationWithMinLotNumberAndTime(long minTime, long minLots) {
		this.logger.trace("Starting simulation run");
		FlowItemCounter generatedLots = new FlowItemCounter();
		addListener(generatedLots);
		long eventCounter = 0L;
		eventList.scheduleEvent(new DummyEvent(model, Long.MAX_VALUE, null, null));
		eventList.setSimulationEndTime(Long.MAX_VALUE);
		while ((this.eventList.size() > 0) && (this.currentSimTime <= minTime)
				|| (generatedLots.getItemCount() < minLots)) {
			final ISimEvent event = this.eventList.getNextEvent();
			if (event.getEventTime() < this.currentSimTime)
				throw new SimulatorEngineException("Event (" + event.getClass() + ") was scheduled before("
						+ event.getEventTime() + ") current simulation time(" + getTime() + ")");
			if ((event.getEventTime() > minTime) && (generatedLots.getItemCount() > minLots)) {
				break;
			} else {
				this.currentSimTime = event.getEventTime();
				event.resolveEvent();
				eventCounter++;
				notifyListener(event);
			}
		}
	}

	public void runSimulationWithMinLotFinishedAndTime(long minTime, long minLots) {
		runSimulationWithMinLotFinishedAndTime(minTime, minLots, 1000 * 60 * 60 * 24 * 10);
	}

	public void runSimulationWithMinLotFinishedAndTime(long minTime, long minLots, long warmup) {
		this.logger.trace("Starting simulation run");
		FinishedFlowItemCounter finishedLots = new FinishedFlowItemCounter();
		addListener(finishedLots);
		long eventCounter = 0L;
		eventList.scheduleEvent(new DummyEvent(model, Long.MAX_VALUE, null, null));
		eventList.scheduleEvent(new EndOfWarmupEvent(model, warmup, null, null));
		eventList.setSimulationEndTime(Long.MAX_VALUE);
		while ((this.eventList.size() > 0) && (this.currentSimTime <= minTime + warmup)
				|| (finishedLots.getItemCount() < minLots)) {
			final ISimEvent event = this.eventList.getNextEvent();
			if (event.getEventTime() < this.currentSimTime)
				throw new SimulatorEngineException("Event (" + event.getClass() + ") was scheduled before("
						+ event.getEventTime() + ") current simulation time(" + getTime() + ")");
			if ((event.getEventTime() > minTime) && (finishedLots.getItemCount() > minLots)) {
				break;
			} else {
				this.currentSimTime = event.getEventTime();
				event.resolveEvent();
				eventCounter++;
				notifyListener(event);
			}
		}
	}

}
