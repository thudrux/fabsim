package de.terministic.fabsim.metamodel.staticcapacityanalysis;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.terministic.fabsim.metamodel.components.ProcessStep;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.ReworkDetails;
import de.terministic.fabsim.metamodel.components.ReworkGate;
import de.terministic.fabsim.metamodel.components.Source;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup;
import de.terministic.fabsim.metamodel.components.equipment.AbstractResource;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.components.equipment.breakdown.IBreakdown;
import de.terministic.fabsim.metamodel.components.equipment.maintenance.IMaintenance;
import de.terministic.fabsim.metamodel.AbstractSource;
import de.terministic.fabsim.metamodel.FabModel;

public class StaticCapacityAnalysis {
	private final Logger logger = LoggerFactory.getILoggerFactory().getLogger("StaticCapacityAnalysis");

	private final long CAPA_HORIZON = 8640000000L;// 100 days
	private LinkedHashMap<AbstractResource, Double> utilisationBasedCapaUsage = new LinkedHashMap<>();
	private LinkedHashMap<AbstractResource, Double> utilisationBasedCapaLoss = new LinkedHashMap<>();
	private LinkedHashMap<AbstractResource, Double> fixedCapaLoss = new LinkedHashMap<>();

	public StaticCapacityAnalysisResult calculateStaticCapacityUtilization(FabModel model) {
		for (AbstractResource resource : model.getComponents().values()) {
			utilisationBasedCapaUsage.put(resource, Double.valueOf(0.0));
			utilisationBasedCapaLoss.put(resource, Double.valueOf(0.0));
			fixedCapaLoss.put(resource, Double.valueOf(0.0));
		}

		// Calculate capa usage of resources by all products
		for (AbstractSource source : model.getSources()) {
			long interarrivalAvg = source.getAvgInterarrivalTime();
			logger.trace("interarrivalAvg: {}", interarrivalAvg);
			double arrivalsDuringCapaHorizon = (CAPA_HORIZON * 1.0) / (1.0 * interarrivalAvg);
			logger.trace("arrivalsDuringCapaHorizon: {}", arrivalsDuringCapaHorizon);

			Recipe recipe = ((Source) source).getProduct().getRecipe();
			calcAndAddCapaForRecipe(arrivalsDuringCapaHorizon, recipe, 0, recipe.size() - 1, 1.0);
		}
		// logger.debug("Capa usage without breakdowns:{}",
		// utilisationBasedCapaUsage);

		// Calculate capa usage of resources by breakdowns
		for (AbstractResource resource : fixedCapaLoss.keySet()) {
//			System.out.println("resource found breakdown: " + resource.getName());
			for (IBreakdown breakdown : resource.getBreakdowns()) {
//				System.out.println("Breakdown found for: " + resource.getName());
				double avgCycleLength = breakdown.getAvgCycleLength() * 1.0;
//				System.out.println("Avg Breakdown length:\t" + breakdown.getAvgCycleLength());
				double avgDownTimePerCycle = breakdown.getAvgDownTimePerCycle() * 1.0;
//				System.out.println("avgDownTimePerCycle:\t" + avgDownTimePerCycle);
				// logger.debug("cycle length:{} , downperCycle: {}",
				// avgCycleLength, avgDownTimePerCycle);
				double numberOfCyclesInCapaHorizon = CAPA_HORIZON / avgCycleLength;
				// logger.debug("number of cycles: {}",
				// numberOfCyclesInCapaHorizon);
				double currentCapa = fixedCapaLoss.get(resource);
				currentCapa += numberOfCyclesInCapaHorizon * avgDownTimePerCycle;
				// logger.debug("Capa usage for breakdowns:{}",
				// numberOfCyclesInCapaHorizon * avgDownTimePerCycle);
//				System.out.println("New fixed CapaLoss: " + CAPA_HORIZON / currentCapa);
//				System.out.println("Breakdown capa loss for: " + resource.getName() + " is " + currentCapa);
				fixedCapaLoss.put(resource, currentCapa);
			}
		}
		// logger.debug("Capa usage with breakdowns:{}",
		// utilisationBasedCapaUsage);

		// Calculate capa usage of resources by maintenance
		for (AbstractResource resource : utilisationBasedCapaLoss.keySet()) {
//			System.out.println("resource found Maint: " + resource.getName());
			for (IMaintenance maint : resource.getMaintenance()) {
				double avgCycleLength = maint.getAvgCycleLength();
//				System.out.println("avgCycleLength:\t" + avgCycleLength);
				double avgDownTimePerCycle = maint.getAvgDownTimePerCycle();
//				System.out.println("avgDownTimePerCycle:\t" + avgDownTimePerCycle);

				double numberOfCyclesInCapaHorizon = CAPA_HORIZON / avgCycleLength;
				double currentCapa = utilisationBasedCapaLoss.get(resource);
				// double currentCapa = fixedCapaUsage.get(resource);
				currentCapa += numberOfCyclesInCapaHorizon * avgDownTimePerCycle;
				utilisationBasedCapaLoss.put(resource, currentCapa);
				// fixedCapaUsage.put(resource, currentCapa);
			}
		}
		// logger.debug("utilisation based Capa usage = {}",
		// utilisationBasedCapaUsage);

		// calculate utilisation percentages
		for (AbstractResource resource : utilisationBasedCapaUsage.keySet()) {
//			System.out.println(
//					"Calc percentages(" + resource.getName() + ") : " + utilisationBasedCapaLoss.get(resource));
			if (utilisationBasedCapaUsage.get(resource) > 0.001) {
//				System.out.println("usage>0: " + utilisationBasedCapaUsage.get(resource));
				// logger.debug("Toolcount:{} {}", resource, ((ToolGroup)
				// resource).getToolCount());
				double resultUtil = ((ToolGroup) resource).getToolCount() * CAPA_HORIZON
						/ utilisationBasedCapaUsage.get(resource);
				// logger.debug("resultUtil: {}", resultUtil);
				utilisationBasedCapaUsage.put(resource, resultUtil);
			}
		}

		for (AbstractResource resource : utilisationBasedCapaLoss.keySet()) {
			if (utilisationBasedCapaLoss.get(resource) > 0.001) {
				// logger.debug("Toolcount:{} {}", resource, ((ToolGroup)
				// resource).getToolCount());
				double resultUtil = ((ToolGroup) resource).getToolCount() * CAPA_HORIZON
						/ utilisationBasedCapaLoss.get(resource);
				// logger.debug("resultUtil: {}", resultUtil);
				utilisationBasedCapaLoss.put(resource, resultUtil);
			}
		}

		AbstractResource maxResource = null;
		double maxUtil = 0.0;
		for (AbstractResource resource : utilisationBasedCapaUsage.keySet()) {
			if (utilisationBasedCapaUsage.get(resource) + utilisationBasedCapaLoss.get(resource) > maxUtil) {
				maxUtil = utilisationBasedCapaUsage.get(resource) + utilisationBasedCapaLoss.get(resource);
				maxResource = resource;
			}
		}
		// logger.debug("maxUtil: {}", maxUtil);

		AbstractHomogeneousResourceGroup bottleneck = (AbstractHomogeneousResourceGroup) maxResource;
		// logger.debug("Tool dedication multiplier is: {} and utilUsage is {}",
		// bottleneck.getDedicationBasedCapaMultiplier(),
		// utilisationBasedCapaUsage.get(maxResource));
//		StaticCapacityAnalysisResult result = new StaticCapacityAnalysisResult(
//				100.0 * bottleneck.getDedicationBasedCapaMultiplier() / utilisationBasedCapaUsage.get(maxResource),
//				100.0 / (CAPA_HORIZON / fixedCapaLoss.get(maxResource)),
//				100.0 / utilisationBasedCapaLoss.get(maxResource));
		StaticCapacityAnalysisResult result = new StaticCapacityAnalysisResult(
				100.0 * bottleneck.getDedicationBasedCapaMultiplier() / utilisationBasedCapaUsage.get(maxResource),
				(fixedCapaLoss.get(maxResource) > 0.0) ? (100.0 / (CAPA_HORIZON / fixedCapaLoss.get(maxResource))) : 0,
				(utilisationBasedCapaLoss.get(maxResource) > 0) ? (100.0 / utilisationBasedCapaLoss.get(maxResource))
						: 0);

		return result;
	}

	private void calcAndAddCapaForRecipe(double arrivalsDuringCapaHorizon, Recipe recipe, int minStep, int maxStep,
			double reworkMultiplier) {
		for (int i = minStep; i <= maxStep; i++) {
			ProcessStep step = recipe.get(i);
			AbstractResource resource = (AbstractResource) step.getComponent();
			if (step.getDetails(ReworkGate.getProcessDetailsKey())!=null) {
				// Removed this section only for Osram Project as they do not
				// consider rework in their capa analysis
				// ReworkDetails details = (ReworkDetails) step.getDetails();
				// calcAndAddCapaForRecipe(arrivalsDuringCapaHorizon, recipe,
				// details.getReworkStepNumber(), i - 1,
				// details.getReworkProbability()
				// + details.getReworkProbability() *
				// details.getReworkProbability());
			} else {
				calcAndAddCapaForStep(arrivalsDuringCapaHorizon, reworkMultiplier, step, resource);
			}

		}
	}

	private void calcAndAddCapaForStep(double arrivalsDuringCapaHorizon, double reworkMultiplier, ProcessStep step,
			AbstractResource resource) {
		double batchMultiplier = 1.0;
		if (step.getBatchDetails() != null) {
			batchMultiplier = 24.0 / (step.getBatchDetails().getMaxBatch() * 1.0);
		}
		double stepCapa = arrivalsDuringCapaHorizon * (step.getAvgDuration() * 1.0) * batchMultiplier;
		double currentCapa = utilisationBasedCapaUsage.get(resource);
		currentCapa += stepCapa * reworkMultiplier;
		utilisationBasedCapaUsage.put(resource, currentCapa);
	}

}
