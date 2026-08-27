package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoBatchFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionRequest;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionResponse;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.externaldispatch.ExternalDispatchException;

public class ExternalDispatchRule extends AbstractDispatchRule {

	private final FIFO fallbackRule = new FIFO();
	private final DispatchProvider provider;
	private final double leadTimeFactor;

	public ExternalDispatchRule(final String name, final DispatchProvider provider,
			final double leadTimeFactor) {
		super(name == null ? "ExternalDispatchRule" : name);
		if (provider == null) {
			throw new IllegalArgumentException("provider must not be null");
		}
		DispatchDecisionRequest.validateLeadTimeFactor(leadTimeFactor);
		this.provider = provider;
		this.leadTimeFactor = leadTimeFactor;
	}

	public ExternalDispatchRule(final DispatchProvider provider, final double leadTimeFactor) {
		this("ExternalDispatchRule", provider, leadTimeFactor);
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items) {
		return this.fallbackRule.getBestItem(items);
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items, final AbstractToolGroup tg,
			final AbstractTool tool) {
		if (items == null || items.isEmpty() || tg == null || tool == null) {
			return this.fallbackRule.getBestItem(items);
		}
		final DispatchDecisionRequest request = DispatchDecisionRequest.capture(tg.getFabModel(), tg, tool, items,
				this.leadTimeFactor);
		final DispatchDecisionResponse response = this.provider.selectDispatchCandidate(request);
		if (response == null) {
			throw new ExternalDispatchException("External dispatch provider returned no decision for tool group "
					+ tg.getName());
		}
		final long selectedId = response.getSelectedFlowItemId();
		for (final AbstractFlowItem item : items) {
			if (item.getId() == selectedId) {
				return item;
			}
		}
		throw new ExternalDispatchException("External dispatch returned invalid flow item id " + selectedId
				+ " for tool group " + tg.getName());
	}

	@Override
	public ArrayList<AbstractFlowItem> sortWithDispatchRule(final ArrayList<AbstractFlowItem> items) {
		return this.fallbackRule.sortWithDispatchRule(items);
	}

	@Override
	public ArrayList<AbstractFlowItem> addItemToList(final AbstractFlowItem item, final ArrayList<AbstractFlowItem> items) {
		return this.fallbackRule.addItemToList(item, items);
	}

	@Override
	public IFlowItemQueue createBatchQueue(final BatchDetails details) {
		return new FifoBatchFlowItemQueue(details);
	}

	@Override
	public IFlowItemQueue createQueue() {
		return new FifoFlowItemQueue();
	}
}
