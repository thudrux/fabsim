package de.terministic.fabsim.components.equipment.dedication;

import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.components.equipment.AbstractTool;

public class UsageUnderDedication {
	private AbstractTool tool;
	public AbstractTool getTool() {
		return tool;
	}

	public void setTool(AbstractTool tool) {
		this.tool = tool;
	}

	public double getUsage() {
		return usage;
	}

	public void setUsage(double usage) {
		this.usage = usage;
	}

	private double usage=0.0;
	
	public UsageUnderDedication(AbstractTool tool){
		this.tool=tool;
	}
	
	

}
