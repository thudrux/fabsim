package de.terministic.fabsim.core;

public class AbstractFixedModelElement extends AbstractModelElement {
	private final String name;

	public AbstractFixedModelElement(FabModel model, final String name) {
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
