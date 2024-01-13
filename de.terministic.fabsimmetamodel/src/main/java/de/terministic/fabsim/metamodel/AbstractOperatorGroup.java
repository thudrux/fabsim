package de.terministic.fabsim.metamodel;

import java.util.ArrayList;
import java.util.List;

import de.terministic.fabsim.core.IFlowItem;

public class AbstractOperatorGroup extends AbstractComponent {
	private final int count;
	private int available;
	private final List<OperatorDemand> unfullfilledDemandList = new ArrayList<>();
	private final List<OperatorDemand> fullfilledDemandList = new ArrayList<>();

	public AbstractOperatorGroup(FabModel model, final String name, final int count) {
		super(model, name);
		this.count = count;
		this.available = count;
	}

	public boolean allocateOperator(final OperatorDemand demand) {
		if (this.available > 0) {
			this.available--;
			this.fullfilledDemandList.add(demand);
			return true;
		} else {
			this.unfullfilledDemandList.add(demand);
			return false;
		}
	}

	public int getCount() {
		return this.count;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void postponeDemand(final OperatorDemand demand) {
		unfullfilledDemandList.remove(demand);
	}

	public void releaseOperator(final OperatorDemand demand) {
		// logger.debug("Start of releaseOperator");
		this.fullfilledDemandList.remove(demand);
		this.available++;
		if (!this.unfullfilledDemandList.isEmpty()) {
			final OperatorDemand oldDemand = this.unfullfilledDemandList.remove(0);
			this.fullfilledDemandList.add(oldDemand);
			// this.logger.debug("Notifying tool of demand fullfillment");
			oldDemand.notifyOfFullfillment();
		}
		// logger.debug("End of releaseOperator");
	}

	@Override
	public void announceFlowItemArrival(IFlowItem item) {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();

	}
}
