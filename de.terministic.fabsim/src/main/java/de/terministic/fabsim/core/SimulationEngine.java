package de.terministic.fabsim.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.statistics.FlowItemCounter;

public class SimulationEngine {

	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private final IEventListManager eventList;
	// private final IEventListManager eventList;
	private long currentSimTime = 0L;
	private FabModel model;
	private final ArrayList<SimEventListener> listenerList = new ArrayList<>();
	private SimEventFactory eventFactory;

	public SimulationEngine() {
		this.eventList = new PriorityQueueEventListManager();
		this.eventFactory = new SimEventFactory();
		this.eventFactory.setSimulationEngine(this);

	}

	public SimulationEngine(IEventListManager eventList) {
		this.eventList = eventList;
		this.eventFactory = new SimEventFactory();
		this.eventFactory.setSimulationEngine(this);

	}

	public void addListener(final SimEventListener listener) {
		this.listenerList.add(listener);
	}

	public List<SimEventListener> getListener() {
		return listenerList;
	}

	public void addListeners(List<SimEventListener> listeners) {
		for (SimEventListener listener : listeners) {
			addListener(listener);
		}
	}

	public SimEventFactory getEventFactory() {
		return this.eventFactory;
	}

	public IEventListManager getEventList() {
		return this.eventList;
	}

	public FabModel getModel() {
		return this.model;
	}

	public long getTime() {
		return this.currentSimTime;
	}

	protected void setTime(long time) {
		this.currentSimTime = time;
	}

	public void init(final FabModel model) {
		this.model = model;
		if (model == null) {
			this.logger.error("FabModel can not be null");
		}
		eventFactory.setFabModel(model);
		model.setupForSimulation(this);
		model.initialize();
	}

	public void notifyListener(final ISimEvent event) {
		for (final SimEventListener listener : this.listenerList) {
			listener.notify(event);
		}
	}

	public void runSimulation(final long endTime) {
		this.logger.trace("Starting simulation run");
		long eventCounter = 0L;
		eventList.scheduleEvent(new DummyEvent(model, Long.MAX_VALUE, null, null));
		eventList.setSimulationEndTime(endTime);
		boolean logStart = false;
		while (this.eventList.size() > 0 && this.currentSimTime <= endTime) {
			final ISimEvent event = this.eventList.getNextEvent();
//			this.logger.info("[{}] Resolving event: {}", event.getEventTime(), event);

			if (event.getEventTime() < this.currentSimTime)
				throw new SimulatorEngineException("Event (" + event.getClass() + ") was scheduled before("
						+ event.getEventTime() + ") current simulation time(" + getTime() + ")");
			this.currentSimTime = event.getEventTime();
			if (this.currentSimTime > endTime) {
				break;
			} else {
//				if ((event.getComponent() != null)) {// &&
//														// (event.getComponent().getName().equals("ToolGroup_0")))
//														// {
//					this.logger.info("[{}] Resolving event: {}", getTime(), event);
//				}
				event.resolveEvent();
				eventCounter++;
				notifyListener(event);
				// if (eventCounter % 1000000 == 0) {
				// logger.trace("At {} there are {} events left in the event
				// list", event.getTime(), eventList.size());
				// }

			}
		}
		this.currentSimTime = endTime;
		// this.logger.trace("Simulation run completed(Number of resolved events
		// was: {} events left in event list: {})",
		// eventCounter, eventList.size());
		// eventList.logEventListState();
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

				if ((event.getComponent().getName().equals("ToolGroup_2"))) {
					// &&(this.currentSimTime > 25173869486L)) {
//					this.logger.trace("[{}] Resolving event: {}", getTime(), event);
				}
				event.resolveEvent();
				eventCounter++;
				notifyListener(event);
				// if (eventCounter % 1000000 == 0) {
				// logger.trace("At {} there are {} events left in the event
				// list", event.getTime(), eventList.size());
				// }

			}
		}
		// this.logger.trace("Simulation run completed(Number of resolved events
		// was: {} events left in event list: {})",
		// eventCounter, eventList.size());
		// eventList.logEventListState();

	}

	public void setEventFactory(final SimEventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

}
