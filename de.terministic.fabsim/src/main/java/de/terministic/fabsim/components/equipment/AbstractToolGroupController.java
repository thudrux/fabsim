package de.terministic.fabsim.components.equipment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.terministic.fabsim.components.Controller;
import de.terministic.fabsim.components.ToolAndItem;
import de.terministic.fabsim.core.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.core.FabModel;

public abstract class AbstractToolGroupController extends AbstractModelElement {

	protected final String NO_BATCH_BATCH_ID = "noBatch";

	public abstract void onBreakdownFinished(BreakdownFinishedEvent event);

	public abstract ToolAndItem selectToolAndItem(final ToolGroup tg, final AbstractTool tool);

	public abstract ToolAndItem selectToolAndItem(final ToolGroup tg, final AbstractFlowItem item);

	public abstract LinkedHashMap<ToolGroup, LinkedHashMap<String, ArrayList<AbstractFlowItem>>> getItemMap();

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