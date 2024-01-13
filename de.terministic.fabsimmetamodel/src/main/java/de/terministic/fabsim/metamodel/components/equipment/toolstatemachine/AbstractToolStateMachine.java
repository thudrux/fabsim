package de.terministic.fabsim.metamodel.components.equipment.toolstatemachine;

import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.BreakdownFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.BreakdownTriggeredEvent;
import de.terministic.fabsim.metamodel.components.equipment.LoadingFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.MaintenanceFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.MaintenanceTriggeredEvent;
import de.terministic.fabsim.metamodel.components.equipment.OperatorFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.ProcessFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.metamodel.components.equipment.SetupFinishedEvent;
import de.terministic.fabsim.metamodel.components.equipment.UnloadingFinishedEvent;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.OperatorDemand;

public class AbstractToolStateMachine {

	public SemiE10EquipmentState getCurrentSemiE10StateOfTool(final AbstractTool abstractTool) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	public void handleFlowItemArrival(final AbstractTool tool, final AbstractFlowItem flowItem) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onBreakdownFinished(final BreakdownFinishedEvent event) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onBreakdownTriggered(final BreakdownTriggeredEvent event) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onLoadingFinished(final LoadingFinishedEvent loadingFinishedEvent) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onMaintenanceFinished(final MaintenanceFinishedEvent event) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onMaintenanceTriggered(final MaintenanceTriggeredEvent maintenanceTriggeredEvent) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onOperatorBecomesAvailable(final OperatorDemand operatorDemand) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onOperatorFinishedProcessing(final OperatorFinishedEvent operatorFinishedEvent) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onProcessFinished(final ProcessFinishedEvent processFinishedEvent) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onSetupFinishedEvent(final SetupFinishedEvent event) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public void onUnloadingFinished(final UnloadingFinishedEvent unloadingFinishedEvent) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	public boolean readyToProcess(final AbstractTool abstractTool) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	public void registerTool(final AbstractTool abstractTool) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

}
