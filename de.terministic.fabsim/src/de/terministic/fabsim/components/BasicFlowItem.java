/*
 *
 * @author    Falk Pappert
 */
package de.terministic.fabsim.components;

import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.FabModel;

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
	public BasicFlowItem(final FabModel model, final Product product) {
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
