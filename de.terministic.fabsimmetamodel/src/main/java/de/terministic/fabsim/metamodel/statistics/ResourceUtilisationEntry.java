package de.terministic.fabsim.metamodel.statistics;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;

public class ResourceUtilisationEntry {
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	private long lastChange = 0L;
	private SemiE10EquipmentState currentState = SemiE10EquipmentState.SB_NO_MATERIAL;
	private HashMap<SemiE10EquipmentState, Long> utilisationMap = new HashMap<>();

	public void update(long currentTime, SemiE10EquipmentState newState) {
		if (!utilisationMap.containsKey(currentState)) {
			utilisationMap.put(currentState, 0L);
		}
		utilisationMap.put(currentState, utilisationMap.get(currentState) + currentTime - lastChange);
		lastChange = currentTime;
		currentState = newState;
	}

	public HashMap<SemiE10EquipmentState, Long> getUtilisationMap() {
		return utilisationMap;
	}

	public void setUtilisationMap(HashMap<SemiE10EquipmentState, Long> utilisationMap) {
		this.utilisationMap = utilisationMap;
	}

	public void init() {
		for (SemiE10EquipmentState state : SemiE10EquipmentState.values()) {
			utilisationMap.put(state, 0L);
		}
	}

}
