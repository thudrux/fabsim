package de.terministic.fabsim.tests.maintenancetests;

import de.terministic.fabsim.components.equipment.maintenance.SimTimeBasedMaintenance;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.duration.AbstractDurationObject;

public class SimTimeBasedMaintenanceTestUmbrella extends SimTimeBasedMaintenance {
	private long mockTime;
	
	public SimTimeBasedMaintenanceTestUmbrella(FabModel model,String name, AbstractDurationObject duration, AbstractDurationObject time) {
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
