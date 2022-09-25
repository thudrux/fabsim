package de.terministic.fabsim.tests.maintenancetests;

import de.terministic.fabsim.components.equipment.maintenance.SimTimeBasedMaintenance;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.duration.IDuration;

public class SimTimeBasedMaintenanceTestUmbrella extends SimTimeBasedMaintenance {
	private long mockTime;
	
	public SimTimeBasedMaintenanceTestUmbrella(FabModel model,String name, IDuration duration, IDuration time) {
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
