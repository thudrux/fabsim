package de.terministic.fabsim.metamodel;

import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;

public abstract class AbstractSource extends AbstractResource {

	public AbstractSource(FabModel model, final String name) {
		super(model, name);
	}

	public abstract long getAvgInterarrivalTime();

	public abstract Product getProduct();

}
