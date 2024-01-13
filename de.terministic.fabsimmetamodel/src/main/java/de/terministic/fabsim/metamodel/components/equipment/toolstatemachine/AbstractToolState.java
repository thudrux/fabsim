/**
 *
 * @author    Falk Pappert
 **/

package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.ISimEvent;
import de.terministic.fabsim.metamodel.OperatorDemand;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.InvalidEventForToolStateException;
import de.terministic.fabsim.metamodel.components.equipment.LoadingFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.OperatorFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.UnloadingFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.IMaintenance;

/**
 * The Class AbstractToolState.
 */
public abstract class AbstractToolState {

	private HashMap<AbstractTool, ProcessStateDetails> stateDetails = new HashMap<>();

	/** The logger. */
	protected final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());

	private final FabModel model;

	public AbstractToolState(final FabModel model) {
		this.model = model;
	}

	/**
	 * Returns whether a tool is able to process a flow item
	 *
	 * @return true, if possible
	 */
	public abstract boolean canProcess();

	/**
	 * Actions which need to be done when an equipment enters a state
	 *
	 * @param tool
	 *            the tool at which the state is entered
	 * @param item
	 *            the flow item involved in entering the state
	 */
	public SemiE10EquipmentState enterState(final AbstractTool tool, final IFlowItem item) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	/**
	 * Actions which need to be done when an equipment enters a state
	 *
	 * @param tool
	 *            the AbstractTool at which the state is entered
	 * @param breakdown
	 *            the IBreakdown because of which the state is entered
	 */
	public SemiE10EquipmentState enterState(final AbstractTool tool, final IBreakdown breakdown) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	/**
	 * Actions which need to be done when an equipment enters a state
	 *
	 * @param tool
	 *            the AbstractTool at which the state is entered
	 * @param maintenance
	 *            the IMaintenance because of which the state is entered
	 */
	public SemiE10EquipmentState enterState(final AbstractTool tool, final IMaintenance maintenance) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	/**
	 * Actions which need to be done when an equipment enters a state
	 *
	 * @param tool
	 *            the AbstractTool at which a new state is entered
	 * @param event
	 *            the ISimEvent because of which a new state is entered
	 */
	public SemiE10EquipmentState enterState(final AbstractTool tool, final ISimEvent event) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	public FabModel getFabModel() {
		return this.model;
	}

	/**
	 * Gets the SEMIE10 state of the tool.
	 *
	 * @return the semi E 10 state
	 */
	public abstract SemiE10EquipmentState getSemiE10State(AbstractTool tool);

	public HashMap<AbstractTool, ProcessStateDetails> getStateDetails() {
		return this.stateDetails;
	}

	/**
	 * Returns whether a event which did not cause a state change should be
	 * triggered as soon as the current state is changed
	 *
	 * @param event
	 *            the event which was not triggered
	 * @return true, if the event in question should be queued and triggered
	 *         again as soon as the tool changes it's current state back to
	 *         SB_NO_MATERIAL
	 */
	public boolean needsEventForLater(final ISimEvent event) {
		return false;
	}

	public AbstractToolState onBreakdownFinished(final AbstractTool tool, final IBreakdown breakdown) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	public AbstractToolState onBreakdownTriggered(final AbstractTool tool, final IBreakdown breakdown) {

		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());
	}

	/**
	 * Actions needed to be done when a flow item is arriving at a tool.
	 *
	 * @param tool
	 *            the AbstractTool at which a flow item is arriving
	 * @param item
	 *            the arriving flow item
	 * @return the new state of the tool
	 */
	public AbstractToolState onFlowItemArrival(final AbstractTool tool, final IFlowItem item) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	public AbstractToolState onLoadingFinished(final LoadingFinishedEvent loadingFinishedEvent) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());
	}

	/**
	 * Actions which need to be done when a maintenance is finished at a tool
	 *
	 * @param tool
	 *            the AbstractTool at which a maintenance was finished
	 * @param maintenance
	 *            the maintenance which was finished
	 * @return the new state of the tool
	 */
	public AbstractToolState onMaintenanceFinished(final AbstractTool tool, final IMaintenance maintenance) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	/**
	 * Actions needed to be done when a maintenance is triggered at a tool.
	 *
	 * @param tool
	 *            the AbstractTool at which a maintenance was triggered
	 * @param maint
	 *            the Maintenance that should be started
	 * @return the new state of the tool
	 */
	public AbstractToolState onMaintenanceTriggered(final AbstractTool tool, final IMaintenance maint) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	public void onOperatorBecomesAvailable(final OperatorDemand operatorDemand) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());
	}

	public void onOperatorFinished(final OperatorFinishedEvent operatorFinishedEvent) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());
	}

	/**
	 * Actions needed to be done when a process is finished at a tool.
	 *
	 * @param tool
	 *            the AbstractTool at which a process is finished
	 * @param item
	 *            the flow item which's process was finished
	 * @return the new state of the tool
	 */
	public AbstractToolState onProcessFinished(final AbstractTool tool, final IFlowItem item) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	/**
	 * Actions needed to be done when a setup is finished at a tool.
	 *
	 * @param tool
	 *            the AbstractTool at which a setup was finished
	 * @param item
	 *            the flow item which caused the setup change
	 * @return the new state of the tool
	 */
	public AbstractToolState onSetupFinished(final AbstractTool tool, final IFlowItem item) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());

	}

	public AbstractToolState onUnloadingFinished(final UnloadingFinishedEvent unloadingFinishedEvent) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());
	}

	public SemiE10EquipmentState resumeState(final AbstractTool tool, final ArrayList<AbstractSimEvent> storedEvents) {
		throw new InvalidEventForToolStateException("Trigger not supported by " + this.getClass().getSimpleName());
	}

	public void setStateDetails(final HashMap<AbstractTool, ProcessStateDetails> stateDetails) {
		this.stateDetails = stateDetails;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}