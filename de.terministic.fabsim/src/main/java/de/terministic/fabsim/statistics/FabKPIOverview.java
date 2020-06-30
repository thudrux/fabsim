package de.terministic.fabsim.statistics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.equipment.AbstractResource;

public class FabKPIOverview {
	private double flowFactor;
	private double avgWIP;
	private long maxWIP;
	private long throughput;
	private Map<AbstractResource, Double> standbyPercentage;
	private double bottleneckUtilisation;
	private long startedLots;
	private long finishedLots;
	private double minCycleTime;
	private double q25CycleTime = 0.0;
	private double avgCycleTime;
	private double q75CycleTime = 0.0;
	private double maxCycleTime;
	private double cycleTimeVariance;
	private double cycleTimeSkewness;
	private double cycleTimeKurtosis;
	private LinkedHashMap<Product, ProductKPIOverview> productKPIs = new LinkedHashMap<>();

	private long prTime;
	private long sbTime;
	private long udTime;
	private long sdmTime;
	private long sdsTime;
	private long totalTime;

	public FabKPIOverview() {
		standbyPercentage = new HashMap<>();
	}

	public String toString() {
		String result = "FabResults: ";
		result += "FF: " + flowFactor;
		result += "|| avgWIP: " + avgWIP;
		result += "|| maxWIP: " + maxWIP;
		result += "|| startedLots: " + startedLots;
		result += "|| finishedLots: " + finishedLots;
		result += "|| totalTime: " + avgWIP;
		return result;
	}

	public double getFlowFactor() {
		return flowFactor;
	}

	public void setFlowFactor(double flowFactor) {
		this.flowFactor = flowFactor;
	}

	public double getAvgWIP() {
		return avgWIP;
	}

	public void setAvgWIP(double avgWIP) {
		this.avgWIP = avgWIP;
	}

	public long getMaxWIP() {
		return maxWIP;
	}

	public void setMaxWIP(long maxWIP) {
		this.maxWIP = maxWIP;
	}

	public double getCycleTimeVariance() {
		return cycleTimeVariance;
	}

	public void setCycleTimeVariance(double cycleTimeVariance) {
		this.cycleTimeVariance = cycleTimeVariance;
	}

	public double getAvgCycleTime() {
		return avgCycleTime;
	}

	public void setAvgCycleTime(double avgCycleTime) {
		this.avgCycleTime = avgCycleTime;
	}

	public void setThroughput(long throughput) {
		this.throughput = throughput;
	}

	public long getThroughput() {
		return throughput;
	}

	public Map<AbstractResource, Double> getStandbyPercentage() {
		return standbyPercentage;
	}

	public void setStandbyPercentage(Map<AbstractResource, Double> standbyPercentage) {
		this.standbyPercentage = standbyPercentage;
	}

	public double getBottleneckUtilisation() {
		return bottleneckUtilisation;
	}

	public void setBottleneckUtilisation(double bottleneckUtilisation) {
		this.bottleneckUtilisation = bottleneckUtilisation;
	}

	public double getCycleTimeSkewness() {
		return cycleTimeSkewness;
	}

	public void setCycleTimeSkewness(double cycleTimeSkewness) {
		this.cycleTimeSkewness = cycleTimeSkewness;
	}

	public double getCycleTimeKurtosis() {
		return cycleTimeKurtosis;
	}

	public void setCycleTimeKurtosis(double cycleTimeKurtosis) {
		this.cycleTimeKurtosis = cycleTimeKurtosis;
	}

	public long getStartedLots() {
		return startedLots;
	}

	public void setStartedLots(long startedLots) {
		this.startedLots = startedLots;
	}

	public long getFinishedLots() {
		return finishedLots;
	}

	public void setFinishedLots(long finishedLots) {
		this.finishedLots = finishedLots;
	}

	public double getMinCycleTime() {
		return minCycleTime;
	}

	public void setMinCycleTime(double minCycleTime) {
		this.minCycleTime = minCycleTime;
	}

	public double getQ25CycleTime() {
		return q25CycleTime;
	}

	public void setQ25CycleTime(double q25CycleTime) {
		this.q25CycleTime = q25CycleTime;
	}

	public double getQ75CycleTime() {
		return q75CycleTime;
	}

	public void setQ75CycleTime(double q75CycleTime) {
		this.q75CycleTime = q75CycleTime;
	}

	public double getMaxCycleTime() {
		return maxCycleTime;
	}

	public void setMaxCycleTime(double maxCycleTime) {
		this.maxCycleTime = maxCycleTime;
	}

	public long getPrTime() {
		return prTime;
	}

	public void setPrTime(long prTime) {
		this.prTime = prTime;
	}

	public long getSbTime() {
		return sbTime;
	}

	public void setSbTime(long sbTime) {
		this.sbTime = sbTime;
	}

	public long getUdTime() {
		return udTime;
	}

	public void setUdTime(long udTime) {
		this.udTime = udTime;
	}

	public long getSdsTime() {
		return sdsTime;
	}

	public long getSdmTime() {
		return sdmTime;
	}

	public void setSdsTime(long sdsTime) {
		this.sdsTime = sdsTime;
	}

	public void setSdmTime(long sdmTime) {
		this.sdmTime = sdmTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	private double wipByDGR;

	public void setWipByDgr(double wipByDGR) {
		this.wipByDGR = wipByDGR;
	}

	public double getWipByDgr() {
		return wipByDGR;
	}

}
