package de.terministic.fabsim.metamodel.components.equipment.maintenance;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.equipment.BreakdownFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.BreakdownTriggeredEvent;
import de.terministic.fabsim.metamodel.components.equipment.ProcessFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.StateChangeEvent;

public class ProcessTimeBasedMaintenanceListener extends SimEventListener {
	private final ProcessTimeBasedMaintenance maint;

	public ProcessTimeBasedMaintenanceListener(final ProcessTimeBasedMaintenance maint) {
		this.maint = maint;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof ProcessFinishedEvent) {
			this.maint.notifyOfProcessFinishedAt(event.getComponent());
		}
		if ((event instanceof StateChangeEvent)
				&& (((StateChangeEvent) event).getNewState().equals(SemiE10EquipmentState.PR))) {
			this.maint.notifyOfProcessStartedAt(event.getComponent());
		}
		if (event instanceof BreakdownTriggeredEvent) {
			this.maint.notifyOfProcessInteruption(event.getComponent());
		}
		if (event instanceof BreakdownFinishedEvent)
		// && (((AbstractResource) ((BreakdownFinishedEvent)
		// event).getComponent())
		// .equals(SemiE10EquipmentState.PR)))

		{
			this.maint.notifyOfProcessEndedInteruption(event.getComponent());
		}
	}

}
