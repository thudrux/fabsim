package de.terministic.fabsim.core;

public interface ISimEventFactory {

	void setModel(IModel model);

	SimulationEngine getSimulationEngine();

	void setSimulationEngine(SimulationEngine engine);

}