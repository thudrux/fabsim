package de.terministic.fabsim.metamodel.components.equipment;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.IMaintenance;

public class MaintenanceFinishedEvent extends AbstractSimEvent {
	private final IMaintenance maint;
	static int count = 0;

	public MaintenanceFinishedEvent(FabModel model, final long time, final AbstractComponent component,
			final IMaintenance maint) {
		super(model, time, component, null);
		this.maint = maint;
		// logger.debug("{} MaintenanceFinishedEvent created: {}", ++count,
		// time);

	}

	public IMaintenance getMaintenance() {
		return this.maint;
	}

	@Override
	public int getPriority() {
		return 7;
	}

	@Override
	public void resolveEvent() {
		super.resolveEvent();
		((AbstractTool) getComponent()).onMaintenanceFinished(this);
	}

}
