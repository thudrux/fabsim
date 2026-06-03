package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoBatchFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.FifoFlowItemQueue;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionClient;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionSnapshot;
import de.terministic.fabsim.metamodel.externaldispatch.ExternalDispatchConfiguration;
import de.terministic.fabsim.metamodel.externaldispatch.ExternalDispatchException;
import de.terministic.fabsim.metamodel.externaldispatch.GrpcDispatchDecisionClient;

public class ExternalDispatchRule extends AbstractDispatchRule {

	private final FIFO fallbackRule = new FIFO();
	private final DispatchDecisionClient client;

	public ExternalDispatchRule(final String name) {
		this(name, ExternalDispatchConfiguration.localDefault());
	}

	public ExternalDispatchRule(final String name, final ExternalDispatchConfiguration configuration) {
		this(name, new GrpcDispatchDecisionClient(configuration));
	}

	public ExternalDispatchRule(final String name, final DispatchDecisionClient client) {
		super(name == null ? "ExternalDispatchRule" : name);
		this.client = client;
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items) {
		return this.fallbackRule.getBestItem(items);
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items, final AbstractToolGroup tg,
			final AbstractTool tool) {
		if (tg == null || tool == null) {
			return this.fallbackRule.getBestItem(items);
		}
		final DispatchDecisionSnapshot snapshot = DispatchDecisionSnapshot.capture(tg.getFabModel(), tg,
				tool, items, getName());
		final long selectedId = this.client.selectFlowItem(snapshot);
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
