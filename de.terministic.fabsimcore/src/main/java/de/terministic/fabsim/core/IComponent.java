package de.terministic.fabsim.core;

public interface IComponent {

	void addListener(SimEventListener listener);

	void initialize();

	long getId();
	
	String getName();

}