package de.terministic.fabsim.metamodel.components.equipment;

import java.util.ArrayList;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.components.Controller;
import de.terministic.fabsim.metamodel.components.ToolAndItem;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;

public class ExternalDesicionToolGroupController extends 
    ToolGroupController{

    public ExternalDesicionToolGroupController(final FabModel model, final Controller controller) {
        super(model, controller);
    }

    
    @Override
      public ToolAndItem selectToolAndItem(final ToolGroup tg, final AbstractTool
      tool) {
      // this.logger.trace("Starting to select item for tool in ToolGroup");
      final AbstractDispatchRule drule = this.getController().getDispatchRule(tg);
      ToolAndItem result = null;
      ArrayList<AbstractFlowItem> possibleItems = new ArrayList<AbstractFlowItem>(
      getPossibleItemsForTheTool(tg, tool));
      // this.logger.debug("start :{} ", possibleItems);
      if (tg.isConsidersDedication()) {
      possibleItems = (ArrayList<AbstractFlowItem>)
      tool.dedicationFilter(possibleItems);
      }
      // this.logger.debug("dedication filter done {}", possibleItems);
      possibleItems = (ArrayList<AbstractFlowItem>)
      tg.getSetupStrategy().filterValidItems(tool, possibleItems);
      // this.logger.debug("setup filter done {}", possibleItems);
      if (possibleItems.size() > 0) {
      final AbstractFlowItem item = drule.getBestItem(possibleItems, tg, tool);
      // this.logger.debug("selected item: {}", possibleItems);
      removeItemFromItemMap(item, tg);
      result = new ToolAndItem(tool, item);
      }
      return result;
 }
    
}
