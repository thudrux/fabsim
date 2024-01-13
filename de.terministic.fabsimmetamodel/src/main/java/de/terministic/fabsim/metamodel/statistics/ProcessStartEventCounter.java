package de.terministic.fabsim.metamodel.statistics;

import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.equipment.ProcessStartEvent;

public class ProcessStartEventCounter extends SimEventListener {
	private long itemCount = 0L;

	public long getItemCount() {
		return this.itemCount;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof ProcessStartEvent) {
			setItemCount(getItemCount() + 1);
		}

	}

	public void setItemCount(final long itemCounter) {
		this.itemCount = itemCounter;
	}

}
