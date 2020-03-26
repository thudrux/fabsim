package de.terministic.fabsim.tests.batchtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.components.LotSource;
import de.terministic.fabsim.components.ProcessStep.ProcessType;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.components.Recipe;
import de.terministic.fabsim.components.Sink;
import de.terministic.fabsim.components.equipment.BatchDetails;
import de.terministic.fabsim.components.equipment.ToolGroup;
import de.terministic.fabsim.core.FabModel;
import de.terministic.fabsim.core.SimulationEngine;
import de.terministic.fabsim.statistics.ItemLog;

public class BatchSplitRevertTest {
	FabModel model;
	SimulationEngine engine;
	LotSource source;
	ToolGroup toolGroup;
	Sink sink;

	@BeforeEach
	public void setUp() throws Exception {
		this.model = new FabModel();
		this.sink = (Sink) this.model.getSimComponentFactory().createSink();
		this.toolGroup = (ToolGroup) this.model.getSimComponentFactory().createToolGroup("Toolgroup", 1,
				de.terministic.fabsim.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType.BATCH);
		final BatchDetails typ1 = this.model.getSimComponentFactory().createBatchDetailsAndAddToToolGroup("typ1", 5, 5,
				this.toolGroup);

		final Recipe recipe = this.model.getSimComponentFactory().createRecipe("Recipe1");
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step1", this.toolGroup, 5L, typ1,
				ProcessType.BATCH, recipe);
		this.model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step2", this.sink, 0L, ProcessType.BATCH,
				recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);
		this.source = (LotSource) this.model.getSimComponentFactory().createSource("Source1", product, 5L);
		this.source.setLotSize(6);
		this.source.setAllowSplit(true);

		this.engine = new SimulationEngine();
	}

	@Test
	public void SplitRevertTest() {
		final ItemLog log = new ItemLog();
		this.engine.init(this.model);
		this.engine.addListener(log);
		this.engine.runSimulation(20L);
		Assertions.assertEquals(6, log.getItems().get(0).getSize());
		Assertions.assertEquals(1, log.getItems().get(0).getId());
	}
}
