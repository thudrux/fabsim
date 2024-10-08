/*
 *
 * @author Falk Pappert
 */

package de.terministic.fabsim.metamodel.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.terministic.fabsim.metamodel.AbstractComponent;
import de.terministic.fabsim.metamodel.AbstractFixedModelElement;
import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.AbstractOperatorGroup;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.core.duration.IValue;
import de.terministic.fabsim.metamodel.components.equipment.BatchDetails;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;

// TODO: Auto-generated Javadoc
/**
 * The Class ProcessStep.
 */
public class ProcessStep extends AbstractFixedModelElement {

	public enum ProcessType {
		WAFER, LOT, BATCH
	};

	/** The component. */
	private final AbstractComponent component;

	/** The batch details. */
	private BatchDetails batchDetails;

	/** The setup details. */
	private SetupState setupDetails;

	/** Programs reflecting this step **/
	private List<Program> programs;

	/** The load time. */
	private final IValue loadTime;

	/** The duration. */
	private IValue duration;

	/** The unload time. */
	private final IValue unloadTime;

	/** Process type has influence on duration calculation */
	private final ProcessType processType;

	/** The operator group used. */
	private AbstractOperatorGroup opGroup;

	public void setOperatorGroup(AbstractOperatorGroup opGroup) {
		this.opGroup = opGroup;
	}

	/**
	 * The avg size of a lot or batch being processed, this value is used to
	 * determine avg processing time for wafer based processing time
	 */
	private long avgContainerSize = 24L;

	public long getAvgContainerSize() {
		return avgContainerSize;
	}

	private LinkedHashMap<String,ProcessDetails> processDetails = new LinkedHashMap<String, ProcessDetails>();

	/**
	 * Instantiates a new process step.
	 *
	 * @param name
	 *            the name
	 * @param component
	 *            the component
	 * @param opGroup
	 *            the operator group
	 * @param loadTime
	 *            the load time
	 * @param duration
	 *            the duration
	 * @param unloadTime
	 *            the unload time
	 * @param batchDetails
	 *            the batch details
	 * @param setupDetails
	 *            the setup details
	 */
	public ProcessStep(FabModel model, final String name, final AbstractComponent component,
			final AbstractOperatorGroup opGroup, final IValue loadTime,
			final IValue duration, final IValue unloadTime,
			final BatchDetails batchDetails, final SetupState setupDetails, final ProcessType type) {
		super(model, name);

		this.component = component;
		this.batchDetails = batchDetails;
		this.setupDetails = setupDetails;
		this.loadTime = loadTime;
		this.duration = duration;
		this.unloadTime = unloadTime;
		this.opGroup = opGroup;
		this.processType = type;
	}

	public void addProgram(final Program program) {
		if (this.programs == null) {
			this.programs = new ArrayList<>();
		}
		this.programs.add(program);
	}

	/**
	 * Gets the batch details.
	 *
	 * @return the batch details
	 */
	public BatchDetails getBatchDetails() {
		return this.batchDetails;
	}

	/**
	 * Gets the component.
	 *
	 * @return the component
	 */
	public AbstractComponent getComponent() {
		return this.component;
	}

	public ProcessDetails getDetails(String key) {
		return this.processDetails.get(key);
	}

	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	public long getDuration(AbstractFlowItem item) {
		if (processType == ProcessType.WAFER) {
			return item.getSize() * this.duration.getValue();
		}
		return this.duration.getValue();
	}

	/**
	 * Gets the load time.
	 *
	 * @return the load time
	 */
	public long getLoadTime() {
		return this.loadTime.getValue();
	}

	/**
	 * Gets the operator group.
	 *
	 * @return the operator group
	 */
	public AbstractOperatorGroup getOperatorGroup() {
		return this.opGroup;
	}

	public List<Program> getPrograms() {
		return this.programs;
	}

	/**
	 * Gets the setup details.
	 *
	 * @return the setup details
	 */
	public SetupState getSetupDetails() {
		return this.setupDetails;
	}

	/**
	 * Gets the unload time.
	 *
	 * @return the unload time
	 */
	public long getUnloadTime() {
		return this.unloadTime.getValue();
	}

	public void setBatchDetails(final BatchDetails batchDetails) {
		this.batchDetails = batchDetails;

	}

	public void addDetails(String key, ProcessDetails details) {
		this.processDetails.put(key, details);
	}

	/**
	 * Sets the duration.
	 *
	 * @param duration
	 *            the new duration
	 */
	public void setDuration(final IValue duration) {
		this.duration = duration;

	}

	public void setPrograms(final List<Program> programs) {
		this.programs = programs;
	}

	public void setSetupDetails(final SetupState setupDetails) {
		this.setupDetails = setupDetails;
	}

	public long getAvgDuration() {
		if (processType == ProcessType.WAFER) {
			return this.avgContainerSize * this.duration.getValue();
		}

		return duration.getAvgValue();
	}

	// @Override
	// public String toString() {
	// return this.getName() + "_" + this.getComponent() + "_" +
	// this.getAvgDuration();
	// }
}
