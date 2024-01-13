package de.terministic.fabsim.metamodel.components.equipment.breakdown;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;

public interface IBreakdown {
	public void addTool(AbstractResource resource);

	public void breakdownFinished(AbstractResource resource);

	public long getDuration();

	public FabModel getModel();

	public String getName();

	public long getTimeOfFirstOccurance(AbstractResource resource);

	public long getTimeOfNextOccurance(AbstractResource resource);

	public long getTimeTillNextOccuranceOnTool(AbstractResource resource);

	public void initialize();

	public void setModel(FabModel model);

	public void setupForSimulation(SimulationEngine engine);

	public double getAvgCycleLength();

	public double getAvgDownTimePerCycle();
}
