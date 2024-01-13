package de.terministic.fabsim.metamodel.components.equipment.maintenance;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.IComponent;
import de.terministic.fabsim.core.duration.IDuration;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;

public class ProcessTimeBasedMaintenance extends AbstractMaintenance implements IMaintenance {
	private final IDuration processTime;
	private final HashMap<AbstractResource, Long> timeSinceLastMaintMap = new LinkedHashMap<>();
	private final HashMap<AbstractResource, Long> lastStartMap = new LinkedHashMap<>();
	private final HashMap<AbstractResource, Long> timeUntilNextMap = new LinkedHashMap<>();
	private final HashMap<AbstractResource, MaintenanceTriggeredEvent> maintForecastMap = new LinkedHashMap<>();
	private final ProcessTimeBasedMaintenanceListener listener;
	private final HashMap<AbstractResource, Boolean> inInteruptionMap = new LinkedHashMap<>();

	public ProcessTimeBasedMaintenance(FabModel model, final String name, final IDuration duration,
			final IDuration processTime) {
		super(model, name, duration);
		this.processTime = processTime;
		this.listener = new ProcessTimeBasedMaintenanceListener(this);
	}

	@Override
	public void addTool(final AbstractResource resource) {
		this.timeSinceLastMaintMap.put(resource, 0L);
		timeUntilNextMap.put(resource, processTime.getDuration());
		inInteruptionMap.put(resource, false);
	}

	@Override
	protected long calculateNextOccuranceOnTool(final AbstractResource resource) {
		return Long.MAX_VALUE;
	}

	@Override
	public long getTimeOfFirstOccurance(final AbstractResource resource) {
		return Long.MAX_VALUE;
	}

	@Override
	public void initialize() {
		logger.trace("timeSinceLastMaintMap: {}", timeSinceLastMaintMap);
		for (final AbstractResource resource : this.timeSinceLastMaintMap.keySet()) {
			// logger.trace("Adding listener to {}", resource);
			listener.addComponentFilter(resource);
			// resource.addListener(this.listener);
		}
		this.getSimulationEngine().addListener(listener);

	}

	@Override
	public void maintenanceFinished(final AbstractResource resource) {
		this.timeSinceLastMaintMap.put(resource, 0L);
		timeUntilNextMap.put(resource, processTime.getDuration());
	}

	public void notifyOfProcessFinishedAt(final IComponent component) {
		// logger.trace(component.getName());
		if (maintenanceForToolIsNotYetTriggered(component)) {
			final AbstractResource resource = (AbstractResource) component;
			// update time total since last maintenance
			// if (component.getName().equals("ToolGroup_5")) {

			final long newTotal = this.timeSinceLastMaintMap.get(resource) + getTime()
					- this.lastStartMap.remove(resource);
			if (component.getName().equals("ToolGroup_5")) {
				logger.trace("updating new total to {} of on avg. {}", newTotal, processTime.getAvgDuration());
			}
			this.timeSinceLastMaintMap.put(resource, newTotal);
			if (maintForecastMap.get(resource).getEventTime() > getTime()) {
				// remove forecast trigger event from eventList

				getSimulationEngine().getEventList().unscheduleEvent(maintForecastMap.get(resource));
			} else {
				this.timeSinceLastMaintMap.put(resource, Long.MIN_VALUE);
			}
			maintForecastMap.remove(resource);
		}
	}

	public void notifyOfProcessInteruption(final IComponent component) {
		logger.trace("[{}] notifyOfProcessInteruption {}", getTime(), component);
		if (component.getName().equals("ToolGroup_0")) {
			logger.trace("\nResource: {} \nTime: {} \nlastStartMap: {} \ntimeSinceLastMaintMap:{}", component,
					getTime(), lastStartMap, timeSinceLastMaintMap);
		}
		if (maintenanceForToolIsNotYetTriggered(component)) {
			final AbstractResource resource = (AbstractResource) component;
			if (maintForecastMap.containsKey(resource)) {
				inInteruptionMap.put(resource, true);
				// update time total since last maintenance
				// logger.trace("Resource: {} Time: {} startMap: {}", resource,
				// getTime(), lastStartMap);
				final long newTotal = this.timeSinceLastMaintMap.get(resource) + getTime()
						- this.lastStartMap.remove(resource);
				// logger.trace("updating new total to {}", newTotal);
				this.timeSinceLastMaintMap.put(resource, newTotal);
				if (maintForecastMap.get(resource).getEventTime() > getTime()) {
					// remove forecast trigger event from eventList
					// logger.trace("Removing Event {}",
					// maintForecastMap.get(resource));
					getSimulationEngine().getEventList().unscheduleEvent(maintForecastMap.get(resource));
				} else {
					this.timeSinceLastMaintMap.put(resource, Long.MIN_VALUE);
				}
				maintForecastMap.remove(resource);
			}
		}
	}

	@Override
	public double getAvgCycleLength() {
		logger.warn("Static capacity analysis is not possible for {}, no influence is assumed",
				this.getClass().getSimpleName());
		return 1.0;
	}

	@Override
	public double getAvgDownTimePerCycle() {
		logger.warn("Static capacity analysis is not possible for {}, no influence is assumed",
				this.getClass().getSimpleName());
		return 0.0;
	}

	public void notifyOfProcessEndedInteruption(IComponent component) {
		if (inInteruptionMap.get(component)) {
			if (maintenanceForToolIsNotYetTriggered(component)) {
				final AbstractTool tool = (AbstractTool) component;

				logger.trace("[{}] notifyOfProcessEndedInteruption {}", getTime(), component);
				this.lastStartMap.put(tool, getTime());
				if (component.getName().equals("ToolGroup_0")) {
					logger.trace("timeUntilNext {}    timeSinceLastMaint {}", this.timeUntilNextMap.get(tool),
							this.timeSinceLastMaintMap.get(tool));
				}
				long timeUntilMaint = this.timeUntilNextMap.get(tool) - this.timeSinceLastMaintMap.get(tool);
				if (component.getName().equals("ToolGroup_0")) {
					logger.trace("[{}] timeUntilMaint {}", getTime(), timeUntilMaint);
				}
				// create MaintTriggeredEvent for Maintenance to come
				MaintenanceTriggeredEvent event = this.model.getEventFactory()
						.scheduleNewMaintenanceTriggeredEvent(timeUntilMaint, tool, this);
				maintForecastMap.put(tool, event);
			}
		}

	}

	private boolean maintenanceForToolIsNotYetTriggered(IComponent component) {
		return timeSinceLastMaintMap.get(component) > Long.MIN_VALUE;
	}

	public void notifyOfProcessStartedAt(final IComponent iComponent) {
		if (iComponent.getName().equals("ToolGroup_3")) {
			logger.trace("process started was notified");
		}

		final AbstractTool tool = (AbstractTool) iComponent;
		if (inInteruptionMap.get(tool)) {
			inInteruptionMap.put(tool, false);
		} else {
			if (maintenanceForToolIsNotYetTriggered(iComponent)) {

				this.lastStartMap.put(tool, getTime());
				long timeUntilMaint = this.timeUntilNextMap.get(tool) - this.timeSinceLastMaintMap.get(tool);

				// create MaintTriggeredEvent for Maintenance to come
				// If condition necessary to avoid error when lot arrival and
				// breakdown happen at the same moment
				if (tool.getCurrentToolState() == SemiE10EquipmentState.PR) {
					MaintenanceTriggeredEvent event = model.getEventFactory()
							.scheduleNewMaintenanceTriggeredEvent(timeUntilMaint, tool, this);
					maintForecastMap.put(tool, event);
				}
			}
		}
	}

}
