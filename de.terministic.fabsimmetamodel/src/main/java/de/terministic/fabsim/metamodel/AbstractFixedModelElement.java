package de.terministic.fabsim.metamodel;

import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.core.IModel;

public class AbstractFixedModelElement extends AbstractModelElement {
	private final String name;

	public AbstractFixedModelElement(IModel model, final String name) {
		super(model);
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
