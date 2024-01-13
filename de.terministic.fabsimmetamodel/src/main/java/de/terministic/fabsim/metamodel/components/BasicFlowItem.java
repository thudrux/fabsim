/*
 *
 * @author    Falk Pappert
 */
package de.terministic.fabsim.metamodel.components;

import de.terministic.fabsim.core.IModel;
import de.terministic.fabsim.metamodel.AbstractFlowItem;

// TODO: Auto-generated Javadoc
/**
 * The Class BasicFlowItem.
 */
public class BasicFlowItem extends AbstractFlowItem {

	/**
	 * Instantiates a new basic flow item.
	 *
	 * @param model
	 *            the model
	 * @param recipe
	 *            the recipe the flow item follows
	 */
	public BasicFlowItem(final IModel model, final Product product) {
		super(model);
		setType(FlowItemType.LOT);
		this.product = product;

		this.setRecipe(product.getRecipe());
	}

	@Override
	public int getSize() {
		return 1;
	}
}
