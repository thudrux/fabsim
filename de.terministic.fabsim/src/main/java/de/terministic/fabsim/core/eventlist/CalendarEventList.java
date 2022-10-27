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

	ArrayList<CalendarQueueBucket> bucketList;
	int nbuckets = 1; // number of buckets
	int qsize = 0; // number of elements in the queue
	double bucketWidth = 1.0;// width of a single bucket;
	int lastBucket;
	long lastprio;
	private int topThreshold;// twice the number of buckets cf. paper
	private int botThreshold;

	@Override
	public AbstractSimEvent getNextEvent() {
		AbstractSimEvent result = null;
		int i;
		if (qsize == 0)
			return null;
		while (result == null) {
			i = lastBucket;
			if (bucketList.get(i).get(0) != null
					&& bucketList.get(i).get(0).getEventTime() < bucketList.get(i).getBucketTop()) {
				result = bucketList.get(i).remove(0);
				lastBucket = i;
				lastprio = result.getEventTime();
				qsize--;
				if (qsize < botThreshold)
					resize(nbuckets / 2);
				return result;
			} else { /*
						 * Prepare to check next bucket or else go to a direct search.
						 */
				++i;
				if (i == nbuckets)
					i = 0;
				bucketList.get(i).setBucketTop(bucketList.get(i).getBucketTop() + bucketWidth);
				if (i == lastBucket)
					break; /* Go to direct search */
			}
		}
		/* Directly search for minimum priority event. */
		// Find lowest priority by examining first event of each
		// bucket;
		// Set lastbucket, lastprio, and buckettop for this event;
		// return(dequeue( 1); / * Resume search at minnode. */
		long highestPrio = Long.MAX_VALUE;
		int bestBucket = lastBucket;
		for (int j = 0; j < nbuckets; j++) {
			int bucketId = lastBucket + j;
			if (bucketId >= nbuckets)
				bucketId -= nbuckets;
			if ((bucketList.get(bucketId).get(0) != null)
					&& (bucketList.get(bucketId).get(0).getEventTime() < highestPrio)) {
				highestPrio = bucketList.get(bucketId).get(0).getEventTime();
				bestBucket = bucketId;
			}
		}
		bestBucket = lastBucket;
		result = bucketList.get(bestBucket).remove(0);
		lastprio = result.getEventTime();
		// TODO update bucketTop
		return result;
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
