package de.terministic.fabsim.core;

import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.equipment.AbstractResource;

public abstract class AbstractSource extends AbstractResource {

	public AbstractSource(FabModel model, final String name) {
		super(model, name);
	}

	public abstract long getAvgInterarrivalTime();

	public abstract Product getProduct();

}
