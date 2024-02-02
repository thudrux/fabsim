package de.terministic.fabsim.metamodel.components;

import java.util.Random;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.NotYetImplementedException;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;

public class ReworkGate extends AbstractResource {
	private static final String processDetailsKey= "REWORK_GATE_PROPABILITY";
	private long reworkCounter = 0L;
	private long testedItems = 0L;
	private Random rand = new Random();

	public void setRandom(Random rand) {
		this.rand = rand;
	}

	public ReworkGate(FabModel model, String name) {
		super(model, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addBreakdown(IBreakdown breakdown) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}

	@Override
	public boolean canProcessItem() {
		return true;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void announceFlowItemArrival(AbstractFlowItem item) {
		// Do nothing rework gate has no capacity limit

	}

	@Override
	public void onFlowItemArrival(FlowItemArrivalEvent event) {
		AbstractFlowItem item = (AbstractFlowItem) event.getFlowItem();
		ReworkDetails details = (ReworkDetails) item.getCurrentStep().getDetails(processDetailsKey);
		setTestedItems(getTestedItems() + 1);
		if (rand.nextDouble() <= details.getReworkProbability()) {
			item.setCurrentStepNumber(details.getReworkStepNumber());
			setReworkCounter(getReworkCounter() + 1);
		} else {
			item.markCurrentStepAsFinished();
		}
		sendFlowItemToResource(item, ((FabModel) getModel()).getRouting());
	}

	public long getReworkCounter() {
		return reworkCounter;
	}

	public void setReworkCounter(long reworkCounter) {
		this.reworkCounter = reworkCounter;
	}

	public long getTestedItems() {
		return testedItems;
	}

	public void setTestedItems(long testedItems) {
		this.testedItems = testedItems;
	}

	public static String getProcessDetailsKey() {
		return processDetailsKey;
	}

}
