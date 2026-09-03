package de.terministic.fabsim.metamodel.externaldispatch;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;
import de.terministic.fabsim.metamodel.dispatchrules.AbstractDispatchRule;

public class LoggingDispatchRule extends AbstractDispatchRule {

	private final AbstractDispatchRule delegate;
	private final LocalLogWriter logWriter;

	public LoggingDispatchRule(final AbstractDispatchRule delegate, final LocalLogWriter logWriter) {
		super(delegate == null ? "LoggingDispatchRule" : delegate.getName());
		if (delegate == null) {
			throw new IllegalArgumentException("delegate must not be null");
		}
		if (logWriter == null) {
			throw new IllegalArgumentException("logWriter must not be null");
		}
		this.delegate = delegate;
		this.logWriter = logWriter;
	}

	public LocalLogWriter getLogWriter() {
		return this.logWriter;
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items) {
		return this.delegate.getBestItem(items);
	}

	@Override
	public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items, final AbstractToolGroup tg,
			final AbstractTool tool) {
		if (items == null || items.isEmpty()) {
			return this.delegate.getBestItem(items, tg, tool);
		}
		final DispatchDecisionRequest request = tg == null || tool == null ? null
				: DispatchDecisionRequest.capture(tg.getFabModel(), tg, tool, new ArrayList<>(items));
		final AbstractFlowItem selectedItem = this.delegate.getBestItem(items, tg, tool);
		if (request != null && selectedItem != null) {
			this.logWriter.append(request, selectedItem);
		}
		return selectedItem;
	}

	@Override
	public ArrayList<AbstractFlowItem> sortWithDispatchRule(final ArrayList<AbstractFlowItem> items) {
		return this.delegate.sortWithDispatchRule(items);
	}

	@Override
	public ArrayList<AbstractFlowItem> sortWithDispatchRule(final ArrayList<AbstractFlowItem> items,
			final AbstractToolGroup tg, final AbstractTool tool) {
		return this.delegate.sortWithDispatchRule(items, tg, tool);
	}

	@Override
	public ArrayList<AbstractFlowItem> addItemToList(final AbstractFlowItem item,
			final ArrayList<AbstractFlowItem> items) {
		return this.delegate.addItemToList(item, items);
	}

	@Override
	public IFlowItemQueue createBatchQueue(final BatchDetails details) {
		return this.delegate.createBatchQueue(details);
	}

	@Override
	public IFlowItemQueue createQueue() {
		return this.delegate.createQueue();
	}
}
