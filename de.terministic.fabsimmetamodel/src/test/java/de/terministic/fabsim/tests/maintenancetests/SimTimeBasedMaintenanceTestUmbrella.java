package de.terministic.fabsim.tests.maintenancetests;

import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.SimTimeBasedMaintenance;

public class SimTimeBasedMaintenanceTestUmbrella extends SimTimeBasedMaintenance {
	private long mockTime;
	
	public SimTimeBasedMaintenanceTestUmbrella(FabModel model,String name, IValue duration, IValue time) {
		super(model,name, duration, time);
	}
	
	@Override
	public long getTime(){
	return mockTime;	
	}
	
	public void setMockTime(long mockTime){
		this.mockTime=mockTime;
	}
}
