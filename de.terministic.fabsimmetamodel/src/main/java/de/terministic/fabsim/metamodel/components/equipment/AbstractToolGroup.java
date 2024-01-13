/*
 *
 * @author    Falk Pappert
 */
package de.terministic.fabsim.metamodel.components.equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.SimEventListener;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;

/**
 * The Class AbstractToolGroup.
 */
public abstract class AbstractToolGroup extends AbstractResourceGroup {

	/** The dispatch rule. */
	protected AbstractDispatchRule dispatchRule;

	/** The fab model. */
	protected FabModel fabModel;

	/** The tg controller. */
	protected AbstractToolGroupController tgController;

	/** The tools. */
	protected Map<Long, AbstractTool> tools;

	/** The tool list. */
	protected List<AbstractResource> toolList = new ArrayList<>();

	protected SetupState currentSetupState;

	/**
	 * Instantiates a new abstract tool group.
	 *
	 * @param name
	 *            the name
	 * @param tgController
	 *            the tg controller
	 */
	public AbstractToolGroup(FabModel model, final String name, final AbstractToolGroupController tgController) {
		super(model, name);
		this.tgController = tgController;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.terministic.alternativefabsimulator.core.AbstractComponent#addListener
	 * (de.terministic.alternativefabsimulator.core.SimEventListener)
	 */
	@Override
	public void addListener(final SimEventListener listener) {
		listener.addComponentFilter(this);
		for (final AbstractTool tool : this.tools.values()) {
			listener.addComponentFilter(tool);
		}
		getSimulationEngine().addListener(listener);
	}

	public abstract void becomesAvailable();

	public abstract void becomesUnavailable();

	/**
	 * Gets the dispatch rule.
	 *
	 * @return the dispatch rule
	 */
	public AbstractDispatchRule getDispatchRule() {
		return this.dispatchRule;
	}

	/**
	 * Gets the fab model.
	 *
	 * @return the fab model
	 */
	public FabModel getFabModel() {
		return this.fabModel;
	}

	/**
	 * Gets the TG controller.
	 *
	 * @return the TG controller
	 */
	public AbstractToolGroupController getTGController() {
		return this.tgController;
	}

	/**
	 * Gets the tool by index.
	 *
	 * @param index
	 *            the index
	 * @return the tool by index
	 */
	public AbstractResource getToolByIndex(final int index) {
		return this.toolList.get(index);
	}

	/**
	 * Gets the tools.
	 *
	 * @return the tools
	 */
	public Map<Long, AbstractTool> getTools() {
		return this.tools;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.terministic.alternativefabsimulator.core.AbstractComponent#initialize(
	 * )
	 */
	@Override
	public void initialize() {
		for (final AbstractTool tool : this.tools.values()) {
			tool.initialize();
		}
	}

	public void setCurrentToolGroupState(final SemiE10EquipmentState currentToolGroupState) {
		this.logger.trace("Starting setCurrentToolGroupState({})", currentToolGroupState);
		if (currentToolGroupState != null) {
			final StateChangeEvent event = new StateChangeEvent(getModel(), getSimulationEngine().getTime(), this,
					currentToolGroupState);
			event.setSetupState(this.currentSetupState);
			getSimulationEngine().getEventList().scheduleEvent(event);
		}
	}

	/**
	 * Sets the tools.
	 *
	 * @param tools
	 *            the tools
	 */
	public void setTools(final Map<Long, AbstractTool> tools) {
		this.tools = tools;
	}

	public List<AbstractResource> getToolList() {
		return toolList;
	}

	@Override
	public void onBreakdownFinished(final BreakdownFinishedEvent event) {
		this.tgController.onBreakdownFinished(event);
	}
}
