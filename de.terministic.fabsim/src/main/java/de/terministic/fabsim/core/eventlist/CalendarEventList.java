package de.terministic.fabsim.core.eventlist;

import java.util.ArrayList;

import de.terministic.fabsim.core.AbstractSimEvent;
import de.terministic.fabsim.core.IEventListManager;

/**
 * Based on paper Calendar queues: A fast O(1) priority queue implementation for
 * the simulation event set problem by Randy Brown in Communications of the ACM
 * 1988|31|10
 * 
 * @author Falk Pappert
 *
 */

public class CalendarEventList implements IEventListManager {

	ArrayList<ArrayList<AbstractSimEvent>> bucketList;
	int nbuckets = 1; // number of buckets
	int qsize = 0; // number of elements in the queue
	int bucketWidth = 1;// width of a single bucket;
	int lastBucket;
	private int topThreshold;

	@Override
	public AbstractSimEvent getNextEvent() {
		int i;
		if (qsize == 0)
			return null;

		return null;
	}

	@Override
	public void scheduleEvent(AbstractSimEvent event) {
		long prio = event.getEventTime();
		int i = (int) (prio / bucketWidth); // finds virtual bucket
		i = i % nbuckets; // finds actual bucket
		bucketList.get(i).add(event);
		bucketList.get(i).sort(new EventComparator());
		qsize++;
		if (qsize > topThreshold)
			resize(2 * nbuckets);
	}

	private void resize(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unscheduleEvent(AbstractSimEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSimulationEndTime(long endTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getSpentTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSpentTime(long spentTime) {
		// TODO Auto-generated method stub

	}

}
