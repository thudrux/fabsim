package de.terministic.fabsim.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.components.equipment.AbstractResource;
import de.terministic.fabsim.components.equipment.SemiE10EquipmentState;
import de.terministic.fabsim.core.SimEventListener;

public class SimulationResultAggregator {
	protected Logger logger = LoggerFactory.getILoggerFactory().getLogger(this.getClass().getName());
	// private ItemLog itemLog;
	private CycleTimeLogger ctLogger;
	private TotalWipLogger wipLogger;
	private ResourceUtilisationLogger resLogger;
	private FinishedFlowItemCounter finishLogger;
	private FlowItemCounter creationLogger;

	public SimulationResultAggregator() {
		// itemLog = new ItemLog();
		ctLogger = new CycleTimeLogger();
		wipLogger = new TotalWipLogger();
		resLogger = new ResourceUtilisationLogger();
		finishLogger = new FinishedFlowItemCounter();
		creationLogger = new FlowItemCounter();

	}

	public ArrayList<SimEventListener> neededListeners() {
		ArrayList<SimEventListener> list = new ArrayList<>();
		list.add(ctLogger);
		list.add(wipLogger);
		list.add(resLogger);
		list.add(finishLogger);
		list.add(creationLogger);
		return list;
	}

	public FabKPIOverview generateResults(long start, long end) {
		long cycleTime = 0L;
		long ctCount = 0L;
		long rptSum = 0L;
		double utilisation;

		/*
		 * for (AbstractFlowItem item : itemLog.getItems()) { if
		 * (item.getStatistics().get(ItemLog.DESTRUCTIONTIMEMARKER) != null) { long ct =
		 * item.getStatistics().get(ItemLog.DESTRUCTIONTIMEMARKER) -
		 * item.getStatistics().get(ItemLog.CREATIONTIMEMARKER); cycleTime += ct;
		 * ctCount++; rptSum += item.getRPT(); } }
		 */
		FabKPIOverview result = new FabKPIOverview();
		result.setCtList(ctLogger.getAllCycleTimes());
		for (Long ct : ctLogger.getAllCycleTimes()) {
			logger.trace("Cycletime is: {}", ct);
			cycleTime += ct;
		}
		// logger.debug("{}", ctLogger.getCycleTimes());
		logger.trace("cycleTime sum is {}", cycleTime);
		ctCount = ctLogger.getAllCycleTimes().size();
		if (ctCount > 0L) {
			result.setAvgCycleTime((cycleTime * 1.0) / (ctCount * 1.0));
			result.setFlowFactor(cycleTime * 1.0 / (ctLogger.getRptSum() * 1.0));
			result.setAvgRPT(ctLogger.getRptSum() * 1.0 / ctLogger.getAllCycleTimes().size() * 1.0);
			ctMoments(result, result.getAvgCycleTime());
			result.setAvgWIP(wipLogger.getAvgLotWip(start, end));
			result.setMaxWIP(wipLogger.getMaxWip());
			result.setThroughput(ctCount);
			double days = (end - start) / 86400000L;
			logger.trace("Days: {}|| Started/Finished {}/{} || DGR: {} || WIP per DGR: {}", days,
					creationLogger.getItemCount(), finishLogger.getItemCount(), ctCount / days,
					wipLogger.getMaxWip() * days / ctCount);
			result.setWipByDgr(wipLogger.getMaxWip() / (1.0 * ctCount / (1.0 * days)));
		} else {
		}
		resLogger.finalizeStatistics(end);
		double maxUtil = 0.0;
		for (AbstractResource resource : resLogger.getResourceUtilisationMap().keySet()) {
			logger.trace("Looking at Resource {} and calculating utilisation", resource);
			long sbTime = resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
					.get(SemiE10EquipmentState.SB_NO_MATERIAL);

			long udTime = 0L;
			if (resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
					.containsKey(SemiE10EquipmentState.UD)) {
				udTime = resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
						.get(SemiE10EquipmentState.UD);
			}
			long sdmTime = 0L;
			if (resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
					.containsKey(SemiE10EquipmentState.SD_MAINT)) {

				sdmTime = resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
						.get(SemiE10EquipmentState.SD_MAINT);
			}
			long sdsTime = 0L;
			if (resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
					.containsKey(SemiE10EquipmentState.SD_SETUP)) {

				sdsTime = resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
						.get(SemiE10EquipmentState.SD_SETUP);
			}
			// logger.debug("Resource: {}", resource);
			// logger.debug("resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap():{}",
			// resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap());
			long prTime = resLogger.getResourceUtilisationMap().get(resource).getUtilisationMap()
					.get(SemiE10EquipmentState.PR);
			// wipLogger.printLog();
			double util = ((end - sbTime) * 1.0) / (1.0 * end);
			if (util > maxUtil) {

				result.setPrTime(prTime);
				result.setSbTime(sbTime);
				result.setUdTime(udTime);
				result.setSdmTime(sdmTime);
				result.setSdsTime(sdsTime);
				result.setTotalTime(end - start);

				maxUtil = util;
			}
		}
		result.setStartedLots(creationLogger.getItemCount());
		result.setFinishedLots(finishLogger.getItemCount());
		utilisation = maxUtil;
		logger.trace("Utilisation of {} calculated", utilisation);
		result.setBottleneckUtilisation(utilisation);

		return result;

	}

	private FabKPIOverview ctMoments(FabKPIOverview result, double meanInMilliSeconds) {
		double variance = 0.0;
		double skewness = 0.0;
		double kurtosis = 0.0;
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		double mean = meanInMilliSeconds / 60000;
		double n = ctLogger.getAllCycleTimes().size() * 1.0;
		double kurtMulti = n * (n + 1) / ((n - 1) * (n - 2) * (n - 3));
		for (Long ctInMSecs : ctLogger.getAllCycleTimes()) {
			if (ctInMSecs < min) {
				min = ctInMSecs;
			} else {
				if (ctInMSecs > max) {
					max = ctInMSecs;
				}
			}
			long ctInMinutes = ctInMSecs / 60000L;
			variance += (ctInMinutes - mean) * (ctInMinutes - mean) / (n - 1.0);
			skewness += n * (ctInMinutes - mean) * (ctInMinutes - mean) * (ctInMinutes - mean)
					/ ((n - 1.0) * (n - 2.0));
			kurtosis += kurtMulti * (ctInMinutes - mean) * (ctInMinutes - mean) * (ctInMinutes - mean)
					* (ctInMinutes - mean);
		}
		double stdDev = Math.sqrt(Math.abs(variance));

		double excess = (kurtosis / (variance * variance)) - (3 * ((n - 1) * (n - 1)) / ((n - 2) * (n - 3)));
		result.setCycleTimeVariance(variance);
		result.setCycleTimeSkewness(skewness / (stdDev * stdDev * stdDev));
		result.setCycleTimeKurtosis(excess);
		result.setMinCycleTime(min);
		result.setMaxCycleTime(max);

		// TODO remove for speed up
		List<Long> ctList = ctLogger.getAllCycleTimes();
		Collections.sort(ctList);
		int q1Pos = ctList.size() / 4;
		int q3Pos = q1Pos * 3;
		result.setQ25CycleTime(ctList.get(q1Pos));
		result.setQ75CycleTime(ctList.get(q3Pos));
		return result;
	}

}
