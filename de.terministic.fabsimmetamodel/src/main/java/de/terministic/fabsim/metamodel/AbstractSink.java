package de.terministic.fabsim.metamodel;

import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;

public abstract class AbstractSink extends AbstractResource {

	public AbstractSink(FabModel model, final String name) {
		super(model, name);
	}

}
