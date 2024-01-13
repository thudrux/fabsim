package de.terministic.fabsim.metamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.core.SimulationEngine;

public abstract class AbstractModelElement {
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private SimulationEngine simEngine;

	private final long id;
	protected final FabModel model;

	protected FabModel getModel() {
		return model;
	}

	public FabSimEventFactory getEventFactory() {
		return model.getEventFactory();
	}
	public AbstractModelElement(FabModel model) {
		this.model = model;
		this.id = model.getNextId();
	}

	public long getId() {
		return this.id;
	}

	protected SimulationEngine getSimulationEngine() {
		return this.simEngine;
	}

	public long getTime() {
		return getSimulationEngine().getTime();
	}

	public void setupForSimulation(final SimulationEngine engine) {
		this.logger.trace("Setting up {} for simulation", this);
		this.simEngine = engine;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "_" + this.id;
	}

}
