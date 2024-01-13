/*
 *
 * @author    Falk Pappert
 */
package de.terministic.fabsim.metamodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.core.AbstractModelElement;
import de.terministic.fabsim.core.IModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.core.duration.DurationFactory;
import de.terministic.fabsim.core.eventlist.ComponentComparator;
import de.terministic.fabsim.metamodel.batchrules.AbstractBatchRule;
import de.terministic.fabsim.metamodel.batchrules.BasicBatchRule;
import de.terministic.fabsim.metamodel.components.BasicOperatorGroup;
import de.terministic.fabsim.metamodel.components.BasicRouting;
import de.terministic.fabsim.metamodel.components.Controller;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractToolGroupController;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroupController;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.metamodel.dispatchRules.AbstractDispatchRule;
import de.terministic.fabsim.metamodel.dispatchRules.FIFO;

public class FabModel implements IModel{
	private String name;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	private long lastFlowItemId;
	private long nextId = 0L;
	private LinkedHashMap<Long, AbstractResource> components;
	private LinkedHashMap<Long, AbstractToolGroup> toolGroups;
	private LinkedHashMap<Long, AbstractOperatorGroup> operators;
	private final LinkedHashMap<Long, AbstractModelElement> elements;

	private LinkedHashMap<String, Recipe> recipes;

	private LinkedHashMap<String, Product> productMap;

	public void addProduct(String name, Product product) {
		productMap.put(name, product);
	}

	private LinkedHashMap<Long, AbstractSource> sourceMap;

	private ArrayList<AbstractSource> sources;

	private LinkedHashMap<Long, AbstractSink> sinks;

	private final ArrayList<IMaintenance> maints;

	private final ArrayList<IBreakdown> breakdowns;

	private final ArrayList<AbstractResource> sortedComponentList;

	private final ComponentComparator compComparator = new ComponentComparator();

	private final AbstractRouting routing;
	protected Controller dispatchController;
	protected AbstractToolGroupController tgController;

	protected AbstractDispatchRule globalDispatchRule;

	protected AbstractBatchRule batchRule;

	private SimComponentFactory componentFactory;

	private final DurationFactory durationFactory;

	private SimulationEngine engine;

	public FabModel() {
		this.setName("FabModel");
		this.componentFactory = new SimComponentFactory(this);
		this.durationFactory = new DurationFactory(this);
		this.components = new LinkedHashMap<>();
		this.toolGroups = new LinkedHashMap<>();
		this.elements = new LinkedHashMap<>();
		this.operators = new LinkedHashMap<>();
		this.productMap = new LinkedHashMap<>();
		this.recipes = new LinkedHashMap<>();
		this.sourceMap = new LinkedHashMap<>();
		this.sources = new ArrayList<>();
		this.sinks = new LinkedHashMap<>();
		this.sortedComponentList = new ArrayList<>();
		this.maints = new ArrayList<>();
		this.breakdowns = new ArrayList<>();
		initController();

		this.routing = new BasicRouting(this);
	}

	protected void initController() {
		this.globalDispatchRule = new FIFO();
		this.batchRule = new BasicBatchRule(this);

		this.dispatchController = new Controller(this, this.globalDispatchRule, this.batchRule);
		this.tgController = new ToolGroupController(this, this.dispatchController);
//		this.tgController = new QueueCentricToolGroupController(this, this.dispatchController); // TODO Just for Testing
		// purposes

		this.getElements().put(this.tgController.getId(), this.tgController);
		this.getElements().put(this.dispatchController.getId(), this.dispatchController);
		this.getElements().put(this.batchRule.getId(), this.batchRule);
	}

	public long getNextId() {
		return nextId++;
	}

	public void addBreakdown(final IBreakdown down) {
		this.breakdowns.add(down);
		down.setModel(this);

	}

	public void addComponent(final AbstractResource component) {
		this.components.put(component.getId(), component);
		this.getElements().put(component.getId(), component);
		this.logger.trace("component was added to list :{}", this.components);
	}

	public void addMaintenance(final IMaintenance maint) {
		this.maints.add(maint);
		maint.setFabModel(this);
	}

	public void addOperatorGroup(final BasicOperatorGroup operator) {
		this.operators.put(operator.getId(), operator);

	}

	public void addRecipe(final Recipe recipe) {
		this.logger.trace("Adding a recipe with the name {}", recipe.getName());
		this.recipes.put(recipe.getName(), recipe);
	}

	public void addSink(final AbstractSink sink) {
		this.logger.trace("Adding a sink with the name {}", sink.getName());
		this.components.put(sink.getId(), sink);
		this.sortedComponentList.add(sink);
		this.sortedComponentList.sort(this.compComparator);
		this.sinks.put(sink.getId(), sink);
	}

	public void addSource(final AbstractSource source) {
		this.logger.trace("Adding a source with the name {}", source.getName());
		this.components.put(source.getId(), source);
		this.sortedComponentList.add(source);
		this.sortedComponentList.sort(this.compComparator);
		this.sourceMap.put(source.getId(), source);
		this.sources.add(source);
	}

	public void addToolGroup(final AbstractToolGroup toolGroup) {
		this.logger.trace("Adding a toolGroup with the name {}", toolGroup.getName());
		this.components.put(toolGroup.getId(), toolGroup);
		this.sortedComponentList.add(toolGroup);
		this.sortedComponentList.sort(this.compComparator);
		this.toolGroups.put(toolGroup.getId(), toolGroup);
	}

	public void addSpecialBlock(final AbstractResource res) {
		this.logger.trace("Adding a special block with the name {}", res.getName());
		this.components.put(res.getId(), res);
		this.sortedComponentList.add(res);
		this.sortedComponentList.sort(this.compComparator);
	}

	public boolean canProcessItem(final AbstractFlowItem item) {
		throw new NotYetImplementedException();
	}

	public LinkedHashMap<Long, AbstractResource> getComponents() {
		return this.components;
	}

	public Controller getDispatchController() {
		return this.dispatchController;
	}

	public DurationFactory getDurationObjectFactory() {
		return this.durationFactory;
	}

	public AbstractDispatchRule getGlobalRule() {
		return this.globalDispatchRule;
	}

	public List<AbstractResource> getGroupMembers() {
		return this.sortedComponentList;
	}

	public long getNextFlowItemId() {
		return ++this.lastFlowItemId;
	}

	public LinkedHashMap<Long, AbstractOperatorGroup> getOperators() {
		return this.operators;
	}

	public LinkedHashMap<String, Recipe> getRecipes() {
		return this.recipes;
	}

	/*
	 * @Deprecated public void addComponent(AbstractComponent component){
	 * logger.trace("Adding a component with the name {}", component.getName());
	 * components.put(component.getId(), component);
	 * sortedComponentList.add(component); sortedComponentList.sort(compComparator);
	 * }
	 *
	 *
	 * @Deprecated public void addComponents(List<AbstractComponent> componentList){
	 * for (AbstractComponent component:componentList){
	 * components.put(component.getId(), component);
	 * sortedComponentList.add(component); }
	 * sortedComponentList.sort(compComparator); }
	 */

	public AbstractRouting getRouting() {
		return this.routing;
	}

	public SimComponentFactory getSimComponentFactory() {
		return this.componentFactory;
	}

	public SimulationEngine getSimulationEngine() {
		return this.engine;
	}

	public LinkedHashMap<Long, AbstractSink> getSinks() {
		return this.sinks;
	}

	public LinkedHashMap<Long, AbstractSource> getSourceMap() {
		return this.sourceMap;
	}

	public ArrayList<AbstractSource> getSources() {
		return this.sources;
	}

	public AbstractToolGroupController getToolGroupController() {
		return this.tgController;
	}

	public LinkedHashMap<Long, AbstractToolGroup> getToolGroups() {
		return this.toolGroups;
	}

	public void initialize() {
		for (final AbstractComponent comp : this.sortedComponentList) {
			comp.initialize();
		}
		for (final IMaintenance maint : this.maints) {
			maint.initialize();
		}
		for (final IBreakdown down : this.breakdowns) {
			down.initialize();
		}
	}

	public void setComponents(final LinkedHashMap<Long, AbstractResource> components) {
		this.components = components;
	}

	public void setGlobalRule(final AbstractDispatchRule rule) {
		this.globalDispatchRule = rule;
	}

	public void setLastFlowItemId(final long id) {
		this.lastFlowItemId = id;
	}

	public void setOperators(final LinkedHashMap<Long, AbstractOperatorGroup> operators) {
		this.operators = operators;
	}

	public void setRecipes(final LinkedHashMap<String, Recipe> recipes) {
		this.recipes = recipes;
	}

	public void setSimulationEngine(final SimulationEngine engine) {
		this.engine = engine;
	}

	public void setSinks(final LinkedHashMap<Long, AbstractSink> sinks) {
		this.sinks = sinks;
	}

	public void setSourceMap(final LinkedHashMap<Long, AbstractSource> sources) {
		this.sourceMap = sources;
	}

	public void setSources(final ArrayList<AbstractSource> sources) {
		this.sources = sources;
	}

	public void setToolGroups(final LinkedHashMap<Long, AbstractToolGroup> toolGroups) {
		this.toolGroups = toolGroups;
	}

	public void setupForSimulation(final SimulationEngine engine) {
		this.logger.trace("Starting setup for Simulation");
		this.engine = engine;
		if (engine == null) {
			this.logger.error("SimulationEngine can not be null");
		}
		this.routing.setupForSimulation(engine);
		for (final IMaintenance maint : this.maints) {
			maint.setupForSimulation(engine);
		}
		for (final IBreakdown breakdown : this.breakdowns) {
			breakdown.setupForSimulation(engine);
		}

		for (final AbstractComponent comp : this.sortedComponentList) {
			comp.setupForSimulation(engine);
		}
		for (final AbstractModelElement el : this.getElements().values()) {
			el.setupForSimulation(engine);
		}
	}

	public void logModel() {
		logger.debug(getName());
		logger.debug("Sources: {}", this.sources);
		logger.debug("Recipies:{}", this.getRecipes().values());
		logger.debug("Tools: {}", this.components.values());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SimComponentFactory getComponentFactory() {
		return componentFactory;
	}

	public void setComponentFactory(SimComponentFactory componentFactory) {
		this.componentFactory = componentFactory;
	}

	public LinkedHashMap<Long, AbstractModelElement> getElements() {
		return elements;
	}

	public FabSimEventFactory getEventFactory() {
		return (FabSimEventFactory) getSimulationEngine().getEventFactory();
	}

}
