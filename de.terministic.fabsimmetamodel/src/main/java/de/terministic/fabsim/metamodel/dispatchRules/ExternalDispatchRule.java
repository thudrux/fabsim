package de.terministic.fabsim.metamodel.dispatchRules;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.queuecentriccontroller.IFlowItemQueue;

public class ExternalDispatchRule extends AbstractDispatchRule {

    public ExternalDispatchRule(final String name) {
        super("ExternalDispatchRule");
    }

    @Override
    public AbstractFlowItem getBestItem(final ArrayList<AbstractFlowItem> items) {
        throw new NotYetImplementedException(
                "ExternalDispatchRule does not yet implement getBestItem(ArrayList<AbstractFlowItem> items)");
        // return null;
    }

    @Override
    public AbstractFlowItem getBestItem(ArrayList<AbstractFlowItem> items,
            AbstractToolGroup tg,
            AbstractTool tool) {
        throw new NotYetImplementedException(
                "ExternalDispatchRule does not yet implement getBestItem(ArrayList<AbstractFlowItem> items)");
        // return null;
    }

    @Override
    public ArrayList<AbstractFlowItem> sortWithDispatchRule(
            ArrayList<AbstractFlowItem> items) {
        throw new NotYetImplementedException(
                "sortWithDispatchRule does not yet implement getBestItem(ArrayList<AbstractFlowItem> items)");
        // return null;
    }

    @Override
    public ArrayList<AbstractFlowItem> addItemToList(
            AbstractFlowItem item, ArrayList<AbstractFlowItem> items) {
        throw new NotYetImplementedException(
                "addItemToList does not yet implement getBestItem(ArrayList<AbstractFlowItem> items)");
        // return null;
    }

    @Override
    public IFlowItemQueue createBatchQueue(
            BatchDetails details) {
        throw new NotYetImplementedException(
                "createBatchQueue does not yet implement getBestItem(ArrayList<AbstractFlowItem> items)");
        // return null;
    }

    @Override
    public IFlowItemQueue createQueue() {
        throw new NotYetImplementedException(
                "createQueue does not yet implement getBestItem(ArrayList<AbstractFlowItem> items)");
        // return null;
    }

}
