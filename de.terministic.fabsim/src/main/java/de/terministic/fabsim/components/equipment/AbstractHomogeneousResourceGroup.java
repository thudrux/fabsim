package de.terministic.fabsim.components.equipment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.terministic.fabsim.batchrules.AbstractBatchRule;
import de.terministic.fabsim.components.InvalidDataException;
import de.terministic.fabsim.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.components.equipment.setup.AbstractSetupStrategy;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.dispatchRules.AbstractDispatchRule;

public abstract class AbstractHomogeneousResourceGroup extends AbstractToolGroup {
	/**
	 * The Enum ProcessingType.
	 */
	public enum ProcessingType {

		/** The batch. */
		BATCH,
		/** The lot. */
		LOT,
		/** The wafer. */
		WAFER
	}

	protected int opLoadPercentage = 0;
	protected int opProcessingPercentage = 0;
	protected int opUnloadPercentage = 0;

	private double dedicationBasedCapaMultiplier = 1.0;
	/** The batch rule. */
	protected AbstractBatchRule batchRule;

	/** The batch details. */
	protected List<BatchDetails> batchDetails;

	/** The setup strategy. */
	protected AbstractSetupStrategy setupStrategy;

	/** The processing type. */
	protected ProcessingType processingType = ProcessingType.LOT;

	/** The setup states. */
	protected Set<SetupState> setupStates = new LinkedHashSet<>();

	/** The setup transitions. */
	protected Map<SetupState, LinkedHashMap<SetupState, Long>> setupTransitions = new LinkedHashMap<>();

	/** The initial setup. */
	protected SetupState initialSetup;

	/** The maintenance map. */
	protected Map<String, IMaintenance> maintenanceMap = new LinkedHashMap<>();

	/** The tool count. */
	private int toolCount;

	private boolean considersDedication = false;

	public AbstractHomogeneousResourceGroup(FabModel model, final String name, final ToolGroupController tgController) {
		super(model, name, tgController);
	}

	/**
	 * Adds a batch detail set.
	 *
	 * @param details
	 *            the details
	 */
	public void addBatchDetails(final BatchDetails details) {
		if (this.batchDetails == null) {
			this.batchDetails = new ArrayList<>();
			this.processingType = ProcessingType.BATCH;
		}
		this.batchDetails.add(details);

	}

	@Override
	public void addBreakdown(final IBreakdown breakdown) {
		this.breakdownMap.put(breakdown.getName(), breakdown);
		for (final AbstractResource tool : this.toolList) {

			breakdown.addTool(tool);
		}
	}

	/**
	 * Adds the maintenance.
	 *
	 * @param maint
	 *            the maint
	 */
	public void addMaintenance(final IMaintenance maint) {
		this.maintenanceMap.put(maint.getName(), maint);
		for (final AbstractResource tool : this.toolList) {
			maint.addTool(tool);
		}
	}

	/**
	 * Adds the setup change.
	 *
	 * @param state1
	 *            the s 1
	 * @param s2
	 *            the s 2
	 * @param setupTime
	 *            the setup time
	 * @param twoWay
	 *            the two way
	 */
	public void addSetupChange(final SetupState state1, final SetupState state2, final Long setupTime,
			final boolean twoWay) {
		if (!this.setupStates.contains(state1))
			throw new InvalidDataException(state1.getSetupName() + " is not known for tool " + getName());
		if (!this.setupStates.contains(state2))
			throw new InvalidDataException(state2.getSetupName() + " is not known for tool " + getName());
		if (setupTime < 0)
			throw new IllegalArgumentException("Setup time must not be negative");
		this.setupTransitions.get(state1).put(state2, setupTime);
		if (twoWay) {
			this.setupTransitions.get(state2).put(state1, setupTime);
		}

	}

	/**
	 * Adds the setup state.
	 *
	 * @param state
	 *            the state
	 */
	public void addSetupState(final SetupState state) {
		this.setupStates.add(state);
		this.setupTransitions.put(state, new LinkedHashMap<SetupState, Long>());
	}

	/**
	 * Gets the batch details.
	 *
	 * @return the batch details
	 */
	public List<BatchDetails> getBatchDetails() {
		return this.batchDetails;
	}

	/**
	 * Gets the batch rule.
	 *
	 * @return the batch rule
	 */
	public AbstractBatchRule getBatchRule() {
		return this.batchRule;
	}

	/**
	 * Gets the initial setup.
	 *
	 * @return the initial setup
	 */
	public SetupState getInitialSetup() {
		return this.initialSetup;
	}

	public int getOpLoadPercentage() {
		return this.opLoadPercentage;
	}

	public int getOpProcessingPercentage() {
		return this.opProcessingPercentage;
	}

	public int getOpUnloadPercentage() {
		return this.opUnloadPercentage;
	}

	/**
	 * Gets the processing type.
	 *
	 * @return the processing type
	 */
	public ProcessingType getProcessingType() {
		return this.processingType;
	}

	/**
	 * Gets the setup states.
	 *
	 * @return the setup states
	 */
	public Set<SetupState> getSetupStates() {
		return this.setupStates;
	}

	/**
	 * Gets the setup strategy.
	 *
	 * @return the setup strategy
	 */
	public AbstractSetupStrategy getSetupStrategy() {
		return this.setupStrategy;
	}

	/**
	 * Gets the setup transitions.
	 *
	 * @return the setup transitions
	 */
	public Map<SetupState, LinkedHashMap<SetupState, Long>> getSetupTransitions() {
		return this.setupTransitions;
	}

	/**
	 * Gets the tool count.
	 *
	 * @return the tool count
	 */
	public int getToolCount() {
		return this.toolCount;
	}

	@Override
	public void initialize() {
		super.initialize();
		this.setupStrategy.initialize(this);
	}

	/**
	 * Sets the batch details.
	 *
	 * @param batchDetails
	 *            the new batch details
	 */
	public void setBatchDetails(final List<BatchDetails> batchDetails) {
		this.batchDetails = batchDetails;
	}

	/**
	 * Sets the batch rule.
	 *
	 * @param rule
	 *            the new batch rule
	 */
	public void setBatchRule(final AbstractBatchRule rule) {
		this.batchRule = rule;
	}

	/**
	 * Sets the dispatch rule.
	 *
	 * @param rule
	 *            the new dispatch rule
	 */
	public void setDispatchRule(final AbstractDispatchRule rule) {
		this.dispatchRule = rule;
	}

	/**
	 * Sets the initial setup.
	 *
	 * @param state
	 *            the new initial setup
	 */
	public void setInitialSetup(final SetupState state) {
		this.initialSetup = state;
		for (final AbstractResource tool : this.toolList) {
			((AbstractTool) tool).setInitialSetupState(state);
		}
	}

	public void setOpLoadPercentage(final int opLoadPercentage) {
		this.opLoadPercentage = opLoadPercentage;
		for (final AbstractResource tool : this.toolList) {
			((AbstractTool) tool).setOpLoadPercentage(opLoadPercentage);
		}
	}

	public void setOpProcessingPercentage(final int opProcessingPercentage) {
		this.opProcessingPercentage = opProcessingPercentage;
		for (final AbstractResource tool : this.toolList) {
			((AbstractTool) tool).setOpProcessingPercentage(opProcessingPercentage);
		}
	}

	public void setOpUnloadPercentage(final int opUnloadPercentage) {
		this.opUnloadPercentage = opUnloadPercentage;
		for (final AbstractResource tool : this.toolList) {
			((AbstractTool) tool).setOpUnloadPercentage(opUnloadPercentage);
		}
	}

	/**
	 * Sets the setup states.
	 *
	 * @param setupStates
	 *            the new setup states
	 */
	public void setSetupStates(final Set<SetupState> setupStates) {
		this.setupStates = setupStates;
	}

	/**
	 * Sets the setup strategy.
	 *
	 * @param setupStrategy
	 *            the new setup strategy
	 */
	public void setSetupStrategy(final AbstractSetupStrategy setupStrategy) {
		this.setupStrategy = setupStrategy;
	}

	/**
	 * Sets the setup transitions.
	 *
	 * @param setupTransitions
	 *            the setup transitions
	 */
	public void setSetupTransitions(final Map<SetupState, LinkedHashMap<SetupState, Long>> setupTransitions) {
		this.setupTransitions = setupTransitions;
	}

	/**
	 * Sets the tool count.
	 *
	 * @param number
	 *            the new tool count
	 */
	public void setToolCount(final int number) {
		this.toolCount = number;
	}

	public boolean isConsidersDedication() {
		return considersDedication;
	}

	public void setConsidersDedication(boolean considersDedication) {
		this.considersDedication = considersDedication;
	}

	public double getDedicationBasedCapaMultiplier() {
		return dedicationBasedCapaMultiplier;
	}

	public void setDedicationBasedCapaMultiplier(double dedicationBasedCapaMultiplier) {
		this.dedicationBasedCapaMultiplier = dedicationBasedCapaMultiplier;
	}

}
