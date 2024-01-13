package de.terministic.fabsim.metamodel.components.equipment;

import java.util.Collection;
import java.util.List;

import de.terministic.fabsim.metamodel.components.Controller;
import de.terministic.fabsim.metamodel.components.ToolAndItem;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.metamodel.FabModel;

public abstract class AbstractToolGroupController extends AbstractModelElement {

	protected final String NO_BATCH_BATCH_ID = "noBatch";

	public abstract void onBreakdownFinished(BreakdownFinishedEvent event);

	public abstract ToolAndItem selectToolAndItem(final ToolGroup tg, final AbstractTool tool);

	public abstract ToolAndItem selectToolAndItem(final ToolGroup tg, final AbstractFlowItem item);

	// public abstract LinkedHashMap<ToolGroup, LinkedHashMap<String,
	// ArrayList<AbstractFlowItem>>> getItemMap();

	public abstract Collection<ToolGroup> getToolGroups();

	public abstract Collection<String> getBatchIdsForToolGroup(ToolGroup toolGroup);

	public abstract Collection<AbstractFlowItem> getQueueForBatchId(ToolGroup toolGroup, String batchId);

	public abstract List<AbstractFlowItem> canUnbatch(final AbstractFlowItem flowItem);

	public abstract void addNewToolGroup(final ToolGroup toolGroup);

	public abstract void addNewItem(final AbstractFlowItem flowItem, final ToolGroup tg);

	protected final Controller controller;

	public AbstractToolGroupController(FabModel model, Controller controller) {
		super(model);
		this.controller = controller;
	}

	public Controller getController() {
		return controller;
	}

}