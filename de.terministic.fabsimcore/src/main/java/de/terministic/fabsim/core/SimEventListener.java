package de.terministic.fabsim.core;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimEventListener {
	private Set<IComponent> componentFilter = new HashSet<>();
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

	public void addComponentFilter(final IComponent abstractComponent) {
		this.componentFilter.add(abstractComponent);

	}

	public void notify(final ISimEvent event) {
		if ((this.componentFilter.isEmpty()) || (this.componentFilter.contains(event.getComponent()))) {
			processEvent(event);
		}
	}

	public abstract void processEvent(ISimEvent event);

	public void setComponentFilter(final Set<IComponent> components) {
		this.componentFilter = components;
	}

	public String toString() {
		return this.getClass().getName() + componentFilter.toString();
	}
}
