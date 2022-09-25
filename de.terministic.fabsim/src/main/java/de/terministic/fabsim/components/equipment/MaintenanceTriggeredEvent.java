package de.terministic.fabsim.components.equipment;

import de.terministic.fabsim.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.core.AbstractComponent;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.FabModel;

public class MaintenanceTriggeredEvent extends AbstractSimEvent {
	IMaintenance maintenance;
	static int count = 0;

	public MaintenanceTriggeredEvent(FabModel model, final long time, final AbstractComponent component,
			final IMaintenance maint) {
		super(model, time, component, null);
		this.maintenance = maint;
		// logger.debug("{} MaintenanceTriggeredEvent created: {}", ++count,
		// time);
	}

	public IMaintenance getMaintenance() {
		return this.maintenance;
	}

	@Override
	public int getPriority() {
		return 6;
	}

	@Override
	public void resolveEvent() {
		// logger.debug("[{}] resolveEvent {}", component.getTime(), this);
		((AbstractTool) this.component).onMaintenanceTriggered(this);
	}

	public void setMaintenance(final IMaintenance maintenance) {
		this.maintenance = maintenance;
	}

}
