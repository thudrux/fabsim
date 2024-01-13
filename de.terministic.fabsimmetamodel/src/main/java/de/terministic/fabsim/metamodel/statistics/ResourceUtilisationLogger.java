package de.terministic.fabsim.metamodel.statistics;

import java.util.HashMap;
import java.util.Map;

import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.StateChangeEvent;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;

public class ResourceUtilisationLogger extends SimEventListener {

	private Map<AbstractResource, ResourceUtilisationEntry> utilisationMap = new HashMap<>();

	@Override
	public void processEvent(ISimEvent event) {
		if (event instanceof StateChangeEvent) {
			final StateChangeEvent sCEvent = (StateChangeEvent) event;
			final AbstractResource resource = (AbstractResource) event.getComponent();
			if (!this.utilisationMap.containsKey(resource)) {
				this.utilisationMap.put(resource, new ResourceUtilisationEntry());
				this.utilisationMap.get(resource).init();
			}
			utilisationMap.get(resource).update(event.getEventTime(), sCEvent.getNewState());
		}
	}

	public Map<AbstractResource, ResourceUtilisationEntry> getResourceUtilisationMap() {
		return utilisationMap;
	}

	public void finalizeStatistics(long end) {
		for (AbstractResource resource : utilisationMap.keySet()) {
			utilisationMap.get(resource).update(end, SemiE10EquipmentState.SB_NO_MATERIAL);
		}
	}

	public Map<AbstractResource, ResourceUtilisationEntry> getUtilisationMap() {
		return utilisationMap;
	}

	public void setUtilisationMap(Map<AbstractResource, ResourceUtilisationEntry> utilisationMap) {
		this.utilisationMap = utilisationMap;
	}

}
