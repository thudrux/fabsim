package de.terministic.fabsim.metamodel.examples;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
public class minifab {

    /**This model is based on the MiniFab Model from researcher from Arizona State Aniversity and Intel. A detailed description can be found in 
    * I. A. El-Khouly, K. S. El-Kilany and A. E. El-Sayed, "Modelling and simulation of re-entrant flow shop scheduling: An application in semiconductor manufacturing," 2009 International Conference on Computers & Industrial Engineering, Troyes, France, 2009, pp. 211-216, doi: 10.1109/ICCIE.2009.5223754.
     * 
    */
    public FabModel createMiniFabModel(){
        FabModel model= new FabModel();
        addToolsToModel(model);
        return model;
    }

    private void addToolsToModel(FabModel model){
        AbstractToolGroup station1 = model.getComponentFactory().createToolGroup("Station1", 2, ProcessingType.BATCH);
        //add batching of 3 lots batch id is based on step id
        //add maintenance 75 minutes every 24 hours for each machine
        model.addComponent(station1);
        AbstractToolGroup station2 = model.getComponentFactory().createToolGroup("Station2", 2, ProcessingType.LOT);
        //add maintenance 120 minutes every 12 hours for each machine
        //add breakdown TODO find exact breakdown distribution for the machine
        model.addComponent(station2);
        AbstractToolGroup station3 = model.getComponentFactory().createToolGroup("Station3", 1, ProcessingType.LOT);
        //add setup time of XXX
        //add maintenance 30 minutes every 12 hours for the machine
        model.addComponent(station3);
    }
}

