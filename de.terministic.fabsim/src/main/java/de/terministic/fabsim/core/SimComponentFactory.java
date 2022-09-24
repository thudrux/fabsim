package de.terministic.fabsim.core;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.components.BasicOperatorGroup;
import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessDetails;
import de.terministic.fabsim.components.ProcessStep;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.ReworkGate;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.components.equipment.AbstractToolGroup;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.SetupState;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.components.equipment.breakdown.SimTimeBasedBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.ItemCountBasedMaintenance;
import de.terministic.fabsim.components.equipment.maintenance.ProcessTimeBasedMaintenance;
import de.terministic.fabsim.components.equipment.maintenance.SimTimeBasedMaintenance;
import de.terministic.fabsim.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.components.equipment.setup.AllAllowedSetupStrategy;
import de.terministic.fabsim.components.equipment.setup.BasicConditionBasedSetupStrategy;
import de.terministic.fabsim.components.equipment.setup.ISetupChangeCondition;
import de.terministic.fabsim.components.equipment.setup.MinItemsInQueueSetupChangeCondition;
import de.terministic.fabsim.components.equipment.setup.MinNumberOfToolsSetupChangeCondition;
import de.terministic.fabsim.components.equipment.setup.MinProducedItemsSinceChangeSetupChangeCondition;
import de.terministic.fabsim.components.equipment.setup.MinProductiveTimeSinceSetupChangeCondition;
import de.terministic.fabsim.components.equipment.toolstatemachine.AbstractToolStateMachine;
import de.terministic.fabsim.components.equipment.toolstatemachine.BasicToolStateMachine;
import de.terministic.fabsim.core.duration.IDuration;
import de.terministic.fabsim.core.duration.ConstantDuration;

public class SimComponentFactory {
	private final FabModel model;
	private AbstractToolStateMachine defaultStateMachine;
	private final AbstractSetupStrategy defaultSetupStrategy = new AllAllowedSetupStrategy();
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getSimpleName());

	public SimComponentFactory(final FabModel model) {
		this.model = model;
		this.defaultStateMachine = new BasicToolStateMachine(model);

	}

	public SimComponentFactory(final FabModel model, AbstractToolStateMachine stateMachine) {
		this.model = model;
		this.defaultStateMachine = stateMachine;

	}

	public AbstractSetupStrategy createAllAllowedSetupStrategyAndAddToToolGroup(
			final AbstractHomogeneousResourceGroup toolGroup) {
		final AbstractSetupStrategy strategy = new AllAllowedSetupStrategy();
		toolGroup.setSetupStrategy(strategy);
		return strategy;
	}

	public AbstractSetupStrategy createBasicConditionBasedSetupStrategyAndAddToToolGroup(
			final AbstractHomogeneousResourceGroup toolGroup) {
		final AbstractSetupStrategy strategy = new BasicConditionBasedSetupStrategy();
		toolGroup.setSetupStrategy(strategy);
		return strategy;
	}

	public BatchDetails createBatchDetailsAndAddToToolGroup(final String name, final int minBatch, final int maxBatch,
			final long maxWait, final ToolGroup toolGroup) {
		final BatchDetails details = new BatchDetails(name, minBatch, maxBatch, maxWait);
		toolGroup.addBatchDetails(details);
		return details;
	}

	public BatchDetails createBatchDetailsAndAddToToolGroup(final String name, final int minBatch, final int maxBatch,
			final ToolGroup toolGroup) {
		final BatchDetails details = new BatchDetails(name, minBatch, maxBatch);
		toolGroup.addBatchDetails(details);
		return details;
	}

	public ItemCountBasedMaintenance createItemBasedMaintenance(final String name,
			final IDuration duration, final int count) {
		final ItemCountBasedMaintenance maint = new ItemCountBasedMaintenance(model, name, duration, count);
		this.model.addMaintenance(maint);
		return maint;
	}

	public ItemCountBasedMaintenance createItemBasedMaintenanceAndAddToToolGroup(final String name,
			final IDuration duration, final int count, final ToolGroup toolGroup) {
		final ItemCountBasedMaintenance maint = new ItemCountBasedMaintenance(model, name, duration, count);
		toolGroup.addMaintenance(maint);
		this.model.addMaintenance(maint);
		return maint;
	}

	public ISetupChangeCondition createMinItemsInQueueSetupChangeConditionAndAddToSetupStrategy(final SetupState state,
			final int itemsInQueue, final AbstractSetupStrategy strategy) {
		final ISetupChangeCondition condition = new MinItemsInQueueSetupChangeCondition(state, itemsInQueue);
		strategy.addPostCondition(condition);
		return condition;
	}

	public ISetupChangeCondition createMinNumberOfToolsSetupChangeConditionAndAddToSetupStrategy(final SetupState state,
			final int minNumberOfTools, final AbstractSetupStrategy strategy) {
		final ISetupChangeCondition condition = new MinNumberOfToolsSetupChangeCondition(state, minNumberOfTools);
		strategy.addPreCondition(condition);
		return condition;
	}

	public ISetupChangeCondition createMinProducedItemsSinceChangeSetupChangeConditionAndAddToSetupStrategy(
			final SetupState state, final int count, final AbstractSetupStrategy strategy) {
		final ISetupChangeCondition condition = new MinProducedItemsSinceChangeSetupChangeCondition(state, count);
		strategy.addPreCondition(condition);
		return condition;
	}

	public ISetupChangeCondition createMinProductiveTimeSinceSetupChangeConditionAndAddToSetupStrategy(
			final SetupState state, final long minProdTime, final AbstractSetupStrategy strategy) {
		final ISetupChangeCondition condition = new MinProductiveTimeSinceSetupChangeCondition(state, minProdTime);
		strategy.addPreCondition(condition);
		return condition;
	}

	public AbstractOperatorGroup createOperatorGroup(final String name, final int count) {
		final BasicOperatorGroup operator = new BasicOperatorGroup(model, name, count);
		this.model.addOperatorGroup(operator);
		return operator;
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component, final long duration,
			ProcessType type) {
		return createProcessStep(name, component, duration, null, null, type);
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component,
			final IDuration duration, ProcessType type) {
		return createProcessStep(name, component, duration, null, null, type);
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component, final long duration,
			final BatchDetails batchDetails, ProcessType type) {
		return createProcessStep(name, component, duration, batchDetails, null, type);
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component,
			final IDuration duration, final BatchDetails batchDetails, ProcessType type) {
		return createProcessStep(name, component, duration, batchDetails, null, type);
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component,
			final IDuration durObj, final BatchDetails batchDetails, final SetupState setupState,
			ProcessType type) {
		final ConstantDuration loadObj = this.model.getDurationObjectFactory().createConstantDurationObject(0L);
		final ConstantDuration unloadObj = this.model.getDurationObjectFactory().createConstantDurationObject(0L);

		final ProcessStep step = new ProcessStep(model, name, component, null, loadObj, durObj, unloadObj, batchDetails,
				setupState, type);
		return step;
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component, final long duration,
			final BatchDetails batchDetails, final SetupState setupState, ProcessType type) {
		final ConstantDuration loadObj = this.model.getDurationObjectFactory().createConstantDurationObject(0L);
		final ConstantDuration durObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(duration);
		final ConstantDuration unloadObj = this.model.getDurationObjectFactory().createConstantDurationObject(0L);

		final ProcessStep step = new ProcessStep(model, name, component, null, loadObj, durObj, unloadObj, batchDetails,
				setupState, type);
		return step;
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component, final long loadTime,
			final long duration, final long unloadTime, ProcessType type) {
		final ConstantDuration loadObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(loadTime);
		final ConstantDuration durObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(duration);
		final ConstantDuration unloadObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(unloadTime);
		return new ProcessStep(model, name, component, null, loadObj, durObj, unloadObj, null, null, type);
	}

	public ProcessStep createProcessStep(final String name, final AbstractComponent component, final long duration,
			final SetupState setupState, ProcessType type) {
		return createProcessStep(name, component, duration, null, setupState, type);
	}

	public ProcessStep createProcessStepAndAddToRecipe(final String name, final AbstractComponent component,
			final AbstractOperatorGroup operatorGroup, final long loadTime, final long duration, final long unloadTime,
			final BatchDetails batchDetails, final SetupState setupState, ProcessType type, final Recipe recipe) {
		this.logger.trace("OperatorGroup needed for step {} is {}", name, operatorGroup);
		final ConstantDuration loadObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(loadTime);
		final ConstantDuration durObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(duration);
		final ConstantDuration unloadObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(unloadTime);

		final ProcessStep step = new ProcessStep(model, name, component, operatorGroup, loadObj, durObj, unloadObj,
				batchDetails, setupState, type);
		recipe.add(step);
		return step;
	}

	public ProcessStep createProcessStepAndAddToRecipe(final String name, final AbstractComponent component,
			final long duration, final BatchDetails batchDetails, ProcessType type, final Recipe recipe) {
		return createProcessStepAndAddToRecipe(name, component, duration, batchDetails, null, type, recipe);
	}

	public ProcessStep createProcessStepAndAddToRecipe(final String name, final AbstractComponent component,
			final long duration, final BatchDetails batchDetails, final SetupState setupState, ProcessType type,
			final Recipe recipe) {
		final ConstantDuration loadObj = this.model.getDurationObjectFactory().createConstantDurationObject(0L);
		final ConstantDuration durObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(duration);
		final ConstantDuration unloadObj = this.model.getDurationObjectFactory().createConstantDurationObject(0L);
		final ProcessStep step = new ProcessStep(model, name, component, null, loadObj, durObj, unloadObj, batchDetails,
				setupState, type);
		recipe.add(step);
		return step;
	}

	public ProcessStep createProcessStepAndAddToRecipe(String name, AbstractResource component, long duration,
			ProcessType type, Recipe recipe, ProcessDetails details) {

		ProcessStep step = createProcessStepAndAddToRecipe(name, component, duration, null, null, type, recipe);
		step.setDetails(details);
		return step;
	}

	public ProcessStep createProcessStepAndAddToRecipe(final String name, final AbstractComponent component,
			final long duration, ProcessType type, final Recipe recipe) {
		return createProcessStepAndAddToRecipe(name, component, duration, null, null, type, recipe);
	}

	public ProcessStep createProcessStepAndAddToRecipe(final String name, final AbstractComponent component,
			final long duration, final SetupState setupState, ProcessType type, final Recipe recipe) {
		return createProcessStepAndAddToRecipe(name, component, duration, null, setupState, type, recipe);
	}

	public ProcessTimeBasedMaintenance createProcessTimeBasedMaintenance(final String name,
			final IDuration duration, final IDuration processTime) {
		final ProcessTimeBasedMaintenance maint = new ProcessTimeBasedMaintenance(model, name, duration, processTime);
		this.model.addMaintenance(maint);
		return maint;
	}

	public ProcessTimeBasedMaintenance createProcessTimeBasedMaintenanceAndAddToToolGroup(final String name,
			final IDuration duration, final IDuration processTime,
			final AbstractHomogeneousResourceGroup toolGroup) {
		final ProcessTimeBasedMaintenance maint = createProcessTimeBasedMaintenance(name, duration, processTime);
		toolGroup.addMaintenance(maint);
		return maint;
	}

	public Recipe createRecipe(final String name) {
		final Recipe recipe = new Recipe(name);
		this.model.addRecipe(recipe);
		return recipe;

	}

	public Recipe createRecipe(final String name, final ArrayList<ProcessStep> steps) {
		final Recipe recipe = createRecipe(name);
		recipe.addAll(steps);
		return recipe;
	}

	public void createSetupChangeAndAddToToolGroup(final SetupState currentState, final SetupState desiredState,
			final long duration, final AbstractHomogeneousResourceGroup toolGroup) {
		toolGroup.addSetupChange(currentState, desiredState, duration, true);
	}

	public void createSetupChangeAndAddToToolGroup(final SetupState currentState, final SetupState desiredState,
			final long duration, final boolean twoWay, final AbstractHomogeneousResourceGroup toolGroup) {
		toolGroup.addSetupChange(currentState, desiredState, duration, twoWay);
	}

	public SetupState createSetupStateAndAddToToolGroup(final String name,
			final AbstractHomogeneousResourceGroup toolGroup) {
		final SetupState state = new SetupState(name);
		toolGroup.addSetupState(state);
		return state;
	}

	public SimTimeBasedBreakdown createSimulationTimeBasedBreakdown(final String name,
			final IDuration mttr, final IDuration mtbf) {
		final SimTimeBasedBreakdown down = new SimTimeBasedBreakdown(model, name, mttr, mtbf);
		this.model.addBreakdown(down);
		return down;
	}

	public SimTimeBasedBreakdown createSimulationTimeBasedBreakdownAndAddToToolGroup(final String name,
			final IDuration mttr, final IDuration mtbf,
			final AbstractHomogeneousResourceGroup toolGroup) {
		final SimTimeBasedBreakdown down = createSimulationTimeBasedBreakdown(name, mttr, mtbf);
		toolGroup.addBreakdown(down);
		return down;
	}

	public SimTimeBasedMaintenance createSimulationTimeBasedMaintenanceAndAddToToolGroup(final String name,
			final IDuration duration, final IDuration processTime,
			final AbstractHomogeneousResourceGroup toolGroup) {
		final SimTimeBasedMaintenance maint = new SimTimeBasedMaintenance(model, name, duration, processTime);
		toolGroup.addMaintenance(maint);
		this.model.addMaintenance(maint);
		return maint;
	}

	public AbstractSink createSink() {
		return createSink("Sink");
	}

	public AbstractSink createSink(final String name) {
		final Sink sink = new Sink(model, name);
		this.model.addSink(sink);
		return sink;
	}

	public AbstractSource createSource(final String name) {
		final AbstractSource source = new LotSource(model, name);
		this.model.addSource(source);
		return source;
	}

	public AbstractSource createSource(final String name, final Product product, final long interarrivalTime) {
		final LotSource source = (LotSource) createSource(name);
		final ConstantDuration interarrivalTimeObj = this.model.getDurationObjectFactory()
				.createConstantDurationObject(interarrivalTime);

		source.setInterArrivalTime(interarrivalTimeObj);
		source.setProduct(product);
		return source;
	}

	public AbstractSource createSource(final String name, final Product product,
			final IDuration interarrivalTimeObj) {
		final LotSource source = (LotSource) createSource(name);
		source.setInterArrivalTime(interarrivalTimeObj);
		source.setProduct(product);
		return source;
	}

	public Product createProduct(String name, Recipe recipe) {
		Product result = new Product(name, recipe);
		this.model.addProduct(name, result);
		return result;
	}

	/*
	 * public TargetCapacityBasedMaintenance
	 * createTargetCapacityBasedMaintenance(final String name, final
	 * AbstractDurationObject duration) { final TargetCapacityBasedMaintenance
	 * maint = new TargetCapacityBasedMaintenance(model, name, duration);
	 * this.model.addMaintenance(maint); return maint; }
	 */
	public AbstractToolGroup createToolGroup(final String name) {
		final int number = 1;
		return createToolGroup(name, number);
	}

	public AbstractToolGroup createToolGroup(final String name, final int number) {
		final ProcessingType procType = ProcessingType.LOT;
		return createToolGroup(name, number, procType);
	}

	public AbstractToolGroup createToolGroup(final String name, final int number, final ProcessingType procType) {
		final AbstractToolStateMachine stateMachine = this.defaultStateMachine;
		return createToolGroup(name, procType, number, stateMachine);
	}

	public AbstractToolGroup createToolGroup(final String name, final ProcessingType processingType, final int number,
			final AbstractToolStateMachine stateMachine) {
		return createToolGroup(name, processingType, number, stateMachine, this.defaultSetupStrategy);
	}

	public AbstractToolGroup createToolGroup(final String name, final ProcessingType processingType, final int number,
			final AbstractToolStateMachine stateMachine, final AbstractSetupStrategy setupStrategy) {
		final AbstractToolGroup toolGroup = new ToolGroup(model, name, processingType, number, stateMachine,
				this.model.getToolGroupController(), setupStrategy);
		this.model.addToolGroup(toolGroup);
		return toolGroup;
	}

	public ReworkGate createReworkGate(final String name) {
		ReworkGate result = new ReworkGate(model, name);
		this.model.addComponent(result);
		return result;
	}

	public AbstractToolStateMachine getDefaultStateMachine() {
		return defaultStateMachine;
	}

	public void setDefaultStateMachine(AbstractToolStateMachine defaultStateMachine) {
		this.defaultStateMachine = defaultStateMachine;
	}

}
