package de.terministic.fabsim.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.components.FlowItemArrivalEvent;
import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.components.equipment.BreakdownFinishedEvent;
import de.terministic.fabsim.components.equipment.LoadingFinishedEvent;
import de.terministic.fabsim.components.equipment.MaintenanceFinishedEvent;
import de.terministic.fabsim.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.components.equipment.OperatorFinishedEvent;
import de.terministic.fabsim.components.equipment.ProcessFinishedEvent;
import de.terministic.fabsim.components.equipment.SetupFinishedEvent;
import de.terministic.fabsim.components.equipment.UnloadingFinishedEvent;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.IMaintenance;

public class SimEventFactory {
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

	SimulationEngine engine;
	FabModel model;

	public void setFabModel(FabModel model) {
		this.model = model;
	}

	public SimulationEngine getSimulationEngine() {
		return this.engine;
	}

	public BreakdownFinishedEvent scheduleNewBreakdownFinishedEvent(final long duration,
			final AbstractResource resource, final IBreakdown breakdown) {
		final BreakdownFinishedEvent event = new BreakdownFinishedEvent(model,
				getSimulationEngine().getTime() + duration, resource, breakdown);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;

	}

	public AbstractSimEvent scheduleNewLoadingFinishedEvent(final long duration, final AbstractResource resource,
			final AbstractFlowItem item) {
		final LoadingFinishedEvent event = new LoadingFinishedEvent(model, getSimulationEngine().getTime() + duration,
				resource, resource.getParent(), item);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public MaintenanceFinishedEvent scheduleNewMaintenanceFinishedEvent(final long duration,
			final AbstractResource resource, final IMaintenance maintenance) {
		final MaintenanceFinishedEvent event = new MaintenanceFinishedEvent(model,
				getSimulationEngine().getTime() + duration, resource, maintenance);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public MaintenanceTriggeredEvent scheduleNewMaintenanceTriggeredEvent(final AbstractResource resource,
			final IMaintenance maintenance) {
		// logger.debug("scheduleNewMaintenanceTriggeredEvent");
		final MaintenanceTriggeredEvent event = new MaintenanceTriggeredEvent(model,
				maintenance.getTimeOfNextOccurance(resource), resource, maintenance);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public MaintenanceTriggeredEvent scheduleNewMaintenanceTriggeredEvent(final long timeUntil,
			final AbstractResource resource, final IMaintenance maintenance) {
		// logger.debug("scheduleNewMaintenanceTriggeredEvent with time until");
		final MaintenanceTriggeredEvent event = new MaintenanceTriggeredEvent(model,
				getSimulationEngine().getTime() + timeUntil, resource, maintenance);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public AbstractSimEvent scheduleNewOperatorFinishedEvent(final long duration, final AbstractResource resource,
			final AbstractFlowItem item) {
		final OperatorFinishedEvent event = new OperatorFinishedEvent(model, getSimulationEngine().getTime() + duration,
				resource, resource.getParent(), item);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public ProcessFinishedEvent scheduleNewProcessFinishedEvent(final long waitTime, final AbstractResource resource,
			final AbstractFlowItem item) {
		final ProcessFinishedEvent event = new ProcessFinishedEvent(model, getSimulationEngine().getTime() + waitTime,
				resource, resource.getParent(), item);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public SetupFinishedEvent scheduleNewSetupFinishedEvent(final long waitTime, final AbstractResource resource,
			final AbstractFlowItem item) {
		final SetupFinishedEvent event = new SetupFinishedEvent(model, getSimulationEngine().getTime() + waitTime,
				resource, resource.getParent(), item);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public AbstractSimEvent scheduleNewUnloadingFinishedEvent(final long unloadTime, final AbstractResource resource,
			final AbstractFlowItem item) {
		final UnloadingFinishedEvent event = new UnloadingFinishedEvent(model,
				getSimulationEngine().getTime() + unloadTime, resource, resource.getParent(), item);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

	public void setSimulationEngine(final SimulationEngine engine) {
		this.engine = engine;
	}

	public FlowItemArrivalEvent scheduleNewFlowItemArrivalEvent(final AbstractComponent receiver,
			final AbstractFlowItem item, AbstractComponent sender) {
		final FlowItemArrivalEvent event = new FlowItemArrivalEvent(model, getSimulationEngine().getTime(), receiver,
				item, sender);
		getSimulationEngine().getEventList().scheduleEvent(event);
		return event;
	}

}
