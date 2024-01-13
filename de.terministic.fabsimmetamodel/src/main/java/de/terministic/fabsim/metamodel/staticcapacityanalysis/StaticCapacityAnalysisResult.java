package de.terministic.fabsim.metamodel.staticcapacityanalysis;

public class StaticCapacityAnalysisResult {

	private final double utilUsage;
	private final double fixedLoss;
	private final double utilLoss;

	public StaticCapacityAnalysisResult(double utilizationBasedCapaUsage, double fixedCapaLoss,
			double utilizationBasedCapaLoss) {
		this.utilUsage = utilizationBasedCapaUsage;
		this.fixedLoss = fixedCapaLoss;
		this.utilLoss = utilizationBasedCapaLoss;
	}

	public double getUtilUsage() {
		return utilUsage;
	}

	public double getFixedLoss() {
		return fixedLoss;
	}

	public double getUtilLoss() {
		return utilLoss;
	}

}
