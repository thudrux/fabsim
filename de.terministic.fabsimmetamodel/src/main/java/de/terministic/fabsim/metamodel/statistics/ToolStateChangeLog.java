package de.terministic.fabsim.metamodel.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.core.IComponent;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.StateChangeEvent;

public class ToolStateChangeLog extends SimEventListener {
	private final HashMap<AbstractTool, List<ToolStateLogEntry>> log = new LinkedHashMap<>();

	@Override
	public void addComponentFilter(final IComponent abstractComponent) {
		super.addComponentFilter(abstractComponent);
		/*
		 * if (abstractComponent instanceof AbstractTool){ AbstractTool tool
		 * =(AbstractTool)abstractComponent; log.put(tool, new
		 * ArrayList<ToolStateLogEntry>()); ToolStateLogEntry initialEntry = new
		 * ToolStateLogEntry(0L, tool, SemiE10ToolState.SB_NO_MATERIAL,
		 * tool.getToolGroup().getInitialSetup());
		 * log.get(tool).add(initialEntry); }
		 */ }

	public HashMap<AbstractTool, List<ToolStateLogEntry>> getLog() {
		return this.log;
	}

	@Override
	public void processEvent(final ISimEvent event) {
		if (event instanceof StateChangeEvent) {
			final StateChangeEvent sCEvent = (StateChangeEvent) event;
			final AbstractTool tool = (AbstractTool) event.getComponent();
			if (!this.log.containsKey(tool)) {
				this.log.put(tool, new ArrayList<ToolStateLogEntry>());
				final ToolStateLogEntry initialEntry = new ToolStateLogEntry(0L, tool,
						SemiE10EquipmentState.SB_NO_MATERIAL, tool.getInitialSetupState());
				this.log.get(tool).add(initialEntry);
			}
			final ToolStateLogEntry entry = new ToolStateLogEntry(event.getEventTime(), tool, sCEvent.getNewState(),
					sCEvent.getSetupState());
			this.log.get(tool).add(entry);
		}

	}
}
