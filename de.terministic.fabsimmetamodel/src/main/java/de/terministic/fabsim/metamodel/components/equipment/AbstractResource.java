package de.terministic.fabsim.metamodel.components.equipment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.OperatorDemand;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.IMaintenance;

public abstract class AbstractResource extends AbstractComponent {

	/** The breakdown map. */
	protected Map<String, IBreakdown> breakdownMap = new LinkedHashMap<>();

	/** The breakdown map. */
	protected Map<String, IMaintenance> maintenanceMap = new LinkedHashMap<>();

	protected AbstractResourceGroup parent;

	public AbstractResource(FabModel model, final String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Adds the breakdown.
	 *
	 * @param breakdown the breakdown
	 */
	public abstract void addBreakdown(final IBreakdown breakdown);

	public abstract boolean canProcessItem();

	public AbstractResourceGroup getParent() {
		return this.parent;
	}

	public void onBreakdownFinished(final BreakdownFinishedEvent event) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onBreakdownTriggered(final BreakdownTriggeredEvent event) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());

	}

	public void onLoadingFinished(final LoadingFinishedEvent loadingFinishedEvent) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onMaintenanceFinished(final MaintenanceFinishedEvent event) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onMaintenanceTriggered(final MaintenanceTriggeredEvent maintenanceTriggeredEvent) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onOperatorBecomesAvailable(final OperatorDemand operatorDemand) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onOperatorFinishedProcessing(final OperatorFinishedEvent operatorFinishedEvent) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onProcessFinished(final ProcessFinishedEvent processFinishedEvent) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onSetupFinished(final SetupFinishedEvent event) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void onUnloadingFinished(final UnloadingFinishedEvent unloadingFinishedEvent) {
		throw new InvalidEventForResourceException("Trigger is not supported by " + this.getClass().getSimpleName());
	}

	public void setParent(final AbstractResourceGroup parent) {
		this.parent = parent;
	}

	protected void sendFlowItemToResource(AbstractFlowItem flowItem, AbstractResource resource) {
		logger.trace("starting to send {} to {}", flowItem, resource);
		resource.announceFlowItemArrival(flowItem);
		((FabModel) getModel()).getEventFactory().scheduleNewFlowItemArrivalEvent(resource, flowItem, this);
	}

	public Collection<IBreakdown> getBreakdowns() {
		return breakdownMap.values();
	}

	public Collection<IMaintenance> getMaintenance() {
		return maintenanceMap.values();
	}

}
