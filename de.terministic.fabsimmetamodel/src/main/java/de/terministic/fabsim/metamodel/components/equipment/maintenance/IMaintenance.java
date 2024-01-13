package de.terministic.fabsim.metamodel.components.equipment.maintenance;

import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.SimulationEngine;

public interface IMaintenance {
	public void addTool(AbstractResource resource);

	public long getDuration();

	public FabModel getFabModel();

	public String getName();

	public long getTimeOfFirstOccurance(AbstractResource resource);

	public long getTimeOfNextOccurance(AbstractResource resource);

	public long getTimeTillNextOccuranceOnTool(AbstractResource resource);

	public void initialize();

	public void maintenanceFinished(AbstractResource resource);

	public void setFabModel(FabModel model);

	public void setupForSimulation(SimulationEngine engine);

	public double getAvgCycleLength();

	public double getAvgDownTimePerCycle();
}
