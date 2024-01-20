package de.terministic.fabsim.metamodel.components.equipment.maintenance;

import java.util.HashMap;
import java.util.LinkedHashMap;

import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IValue;

public class ItemCountBasedMaintenance extends AbstractMaintenance {
	private final int count;
	private final ItemCountBasedMaintenanceListener listener;
	private final HashMap<AbstractResource, Integer> finishedCountMap;

	public ItemCountBasedMaintenance(FabModel model, final String name, final IValue duration,
			final int count) {
		super(model, name, duration);
		this.count = count;
		this.finishedCountMap = new LinkedHashMap<>();
		this.listener = new ItemCountBasedMaintenanceListener(this);
	}

	@Override
	public void addTool(final AbstractResource resource) {
		this.finishedCountMap.put(resource, 0);
	}

	@Override
	protected long calculateNextOccuranceOnTool(final AbstractResource resource) {
		return Long.MAX_VALUE;
	}

	public int getCount() {
		return this.count;
	}

	@Override
	public long getDuration() {
		return this.duration.getValue();
	}

	@Override
	public long getTimeOfFirstOccurance(final AbstractResource resource) {
		return Long.MAX_VALUE;
	}

	@Override
	public long getTimeOfNextOccurance(final AbstractResource resource) {
		return getTime();
	}

	@Override
	public void initialize() {
		for (final AbstractResource resource : this.finishedCountMap.keySet()) {
			resource.addListener(this.listener);
		}
	}

	@Override
	public void maintenanceFinished(final AbstractResource resource) {
		this.finishedCountMap.put(resource, 0);
	}

	public void notifyOfProcessFinishedAt(final AbstractComponent component) {
		final AbstractTool tool = (AbstractTool) component;
		this.finishedCountMap.put(tool, this.finishedCountMap.get(tool) + 1);
	}

	public void notifyOfProcessStartedAt(final AbstractComponent component) {
		final AbstractResource resource = (AbstractResource) component;
		if (this.finishedCountMap.get(resource) >= this.count - 1) {
			this.model.getEventFactory().scheduleNewMaintenanceTriggeredEvent(resource, this);
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

}
