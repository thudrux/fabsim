package de.terministic.fabsim.core;

public interface IModel {

	SimulationEngine getSimulationEngine();

	void initialize();

	void setSimulationEngine(SimulationEngine engine);

	void setupForSimulation(SimulationEngine engine);
	
	long getNextId();

	long getNextFlowItemId();

}