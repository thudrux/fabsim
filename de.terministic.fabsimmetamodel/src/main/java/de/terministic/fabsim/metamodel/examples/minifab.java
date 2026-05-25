package de.terministic.fabsim.metamodel.examples;

import de.terministic.fabsim.core.duration.ConstantDurationObject;
import de.terministic.fabsim.core.duration.ConstantValue;
import de.terministic.fabsim.core.duration.ExponentialDuration;
import de.terministic.fabsim.core.duration.ExponentialDurationObject;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;

public class MiniFab {
private static long SECOND = 1000L;
private static long MINUTE = 60*SECOND;
private static long HOUR = 60 * MINUTE;
private static long DAY = 24 * HOUR;

    /**
     * This model is based on the MiniFab Model from researcher from Arizona State
     * Aniversity and Intel. A detailed description can be found in
     * I. A. El-Khouly, K. S. El-Kilany and A. E. El-Sayed, "Modelling and
     * simulation of re-entrant flow shop scheduling: An application in
     * semiconductor manufacturing," 2009 International Conference on Computers &
     * Industrial Engineering, Troyes, France, 2009, pp. 211-216, doi:
     * 10.1109/ICCIE.2009.5223754.
     * 
     */
    public FabModel createMiniFabModel() {
        FabModel model = new FabModel();
        addToolsToModel(model);
        return model;
    }

    private void addToolsToModel(FabModel model) {
        ToolGroup station1 = (ToolGroup)model.getComponentFactory().createToolGroup("Station1", 2, ProcessingType.BATCH);
        // add maintenance 75 minutes every 24 hours for each machine
        ExponentialDuration timeToMaintenace1 = model.getValueObjectFactory().createExponentialValueObject(1*DAY);
        ConstantValue timeOfMaintenace1 = model.getValueObjectFactory().createConstantValueObject(75*MINUTE);
        model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup("Maint_Station1", timeOfMaintenace1, timeToMaintenace1,station1);

        // add batching of 3 lots batch id is based on step id
        model.addComponent(station1);
        ToolGroup station2 = (ToolGroup)model.getComponentFactory().createToolGroup("Station2", 2, ProcessingType.LOT);
        // add maintenance 120 minutes every 12 hours for each machine
        ExponentialDuration timeToMaintenace2 = model.getValueObjectFactory().createExponentialValueObject(12*HOUR);
        ConstantValue timeOfMaintenace2 = model.getValueObjectFactory().createConstantValueObject(120*MINUTE);
        model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup("Maint_Station2", timeOfMaintenace2, timeToMaintenace2,station2);
        // add breakdown TODO find exact breakdown distribution for the machine
        model.addComponent(station2);
        ToolGroup station3 = (ToolGroup)model.getComponentFactory().createToolGroup("Station3", 1, ProcessingType.LOT);
        // add setup time of XXX
        // add maintenance 30 minutes every 12 hours for the machine
        ExponentialDuration timeToMaintenace3 = model.getValueObjectFactory().createExponentialValueObject(12*HOUR);
        ConstantValue timeOfMaintenace3 = model.getValueObjectFactory().createConstantValueObject(30*MINUTE);
        model.getSimComponentFactory().createSimulationTimeBasedMaintenanceAndAddToToolGroup("Maint_Station3", timeOfMaintenace3, timeToMaintenace3,station3);

        model.addComponent(station3);
    }
}
