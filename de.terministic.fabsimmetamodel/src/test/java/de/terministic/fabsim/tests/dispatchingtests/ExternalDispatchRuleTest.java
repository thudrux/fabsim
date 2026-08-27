package de.terministic.fabsim.tests.dispatchingtests;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.terministic.fabsim.metamodel.AbstractFlowItem;
import de.terministic.fabsim.metamodel.FabModel;
import de.terministic.fabsim.metamodel.TimeStamps;
import de.terministic.fabsim.metamodel.components.Batch;
import de.terministic.fabsim.metamodel.components.Lot;
import de.terministic.fabsim.metamodel.components.ProcessStep.ProcessType;
import de.terministic.fabsim.metamodel.components.Product;
import de.terministic.fabsim.metamodel.components.Recipe;
import de.terministic.fabsim.metamodel.components.equipment.AbstractHomogeneousResourceGroup.ProcessingType;
import de.terministic.fabsim.metamodel.components.equipment.AbstractTool;
import de.terministic.fabsim.metamodel.components.equipment.SetupState;
import de.terministic.fabsim.metamodel.components.equipment.ToolGroup;
import de.terministic.fabsim.metamodel.dispatchRules.ExternalDispatchRule;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionRequest;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchDecisionResponse;
import de.terministic.fabsim.metamodel.externaldispatch.DispatchProvider;
import de.terministic.fabsim.metamodel.externaldispatch.ExternalDispatchException;

public class ExternalDispatchRuleTest {

	@Test
	public void selectsItemChosenByExternalProvider() {
		TestFixture fixture = createFixture();
		RecordingProvider provider = new RecordingProvider(fixture.secondItem.getId());
		ExternalDispatchRule rule = new ExternalDispatchRule("External", provider, 2.0d);

		AbstractFlowItem selected = rule.getBestItem(fixture.items, fixture.toolGroup, fixture.tool);

		Assertions.assertEquals(fixture.secondItem, selected);
		Assertions.assertEquals(1, provider.callCount);
		Assertions.assertEquals(2, provider.request.getCandidates().size());
		Assertions.assertEquals(fixture.firstItem.getId(), provider.request.getCandidates().get(0).getId());
		Assertions.assertEquals(fixture.secondItem.getId(), provider.request.getCandidates().get(1).getId());
		Assertions.assertEquals("Recipe", provider.request.getCandidates().get(0).getRecipe());
		Assertions.assertEquals(10L, provider.request.getCandidates().get(0).getExpectedProcessingTime());
		Assertions.assertEquals(20L, provider.request.getCandidates().get(0).getRemainingCycleTime());
		Assertions.assertEquals(7L, provider.request.getCandidates().get(0).getExpectedSetupTime());
		Assertions.assertEquals(1, provider.request.getFabState().getToolGroups().size());
		Assertions.assertTrue(provider.request.getFabState().getToolGroups().get(0).getWaitingForDispatch());
	}

	@Test
	public void selectsBatchChosenByExternalProvider() {
		TestFixture fixture = createFixture();
		Batch batch = new Batch(fixture.model, fixture.firstItem.getRecipe());
		batch.addItem(fixture.firstItem);
		batch.addItem(fixture.secondItem);

		ArrayList<AbstractFlowItem> items = new ArrayList<AbstractFlowItem>();
		items.add(batch);
		RecordingProvider provider = new RecordingProvider(batch.getId());
		ExternalDispatchRule rule = new ExternalDispatchRule(provider, 1.0d);

		AbstractFlowItem selected = rule.getBestItem(items, fixture.toolGroup, fixture.tool);

		Assertions.assertEquals(batch, selected);
		Assertions.assertEquals(batch.getId(), provider.request.getCandidates().get(0).getId());
	}

	@Test
	public void rejectsMissingProviderDecision() {
		TestFixture fixture = createFixture();
		ExternalDispatchRule rule = new ExternalDispatchRule(request -> null, 1.0d);

		Assertions.assertThrows(ExternalDispatchException.class,
				() -> rule.getBestItem(fixture.items, fixture.toolGroup, fixture.tool));
	}

	@Test
	public void rejectsUnknownFlowItemIdFromProvider() {
		TestFixture fixture = createFixture();
		ExternalDispatchRule rule = new ExternalDispatchRule(request -> DispatchDecisionResponse.of(-1L), 1.0d);

		Assertions.assertThrows(ExternalDispatchException.class,
				() -> rule.getBestItem(fixture.items, fixture.toolGroup, fixture.tool));
	}

	@Test
	public void validatesConstructorArguments() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ExternalDispatchRule(null, 1.0d));
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> new ExternalDispatchRule(request -> DispatchDecisionResponse.of(1L), 0.0d));
	}

	private TestFixture createFixture() {
		FabModel model = new FabModel();
		ToolGroup toolGroup = (ToolGroup) model.getSimComponentFactory().createToolGroup("ToolGroup", 1,
				ProcessingType.LOT);
		AbstractTool tool = toolGroup.getTools().values().iterator().next();

		SetupState currentSetup = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("Current",
				toolGroup);
		SetupState recipeSetup = model.getSimComponentFactory().createSetupStateAndAddToToolGroup("RecipeSetup",
				toolGroup);
		model.getSimComponentFactory().createSetupChangeAndAddToToolGroup(currentSetup, recipeSetup, 7L,
				toolGroup);
		toolGroup.setInitialSetup(currentSetup);

		Recipe recipe = model.getSimComponentFactory().createRecipe("Recipe");
		model.getSimComponentFactory().createProcessStepAndAddToRecipe("Step", toolGroup, 10L, recipeSetup,
				ProcessType.LOT, recipe);
		Product product = model.getSimComponentFactory().createProduct("Product", recipe);

		Lot firstItem = new Lot(model, product, 1, 1, 50L);
		firstItem.getTimeStampMap().put(0, timeStamps(0L));
		Lot secondItem = new Lot(model, product, 1, 2, 50L);
		secondItem.getTimeStampMap().put(0, timeStamps(5L));

		ArrayList<AbstractFlowItem> items = new ArrayList<AbstractFlowItem>();
		items.add(firstItem);
		items.add(secondItem);
		toolGroup.getQueue().addAll(items);

		return new TestFixture(model, toolGroup, tool, firstItem, secondItem, items);
	}

	private TimeStamps timeStamps(final long arrivalTime) {
		TimeStamps timeStamps = new TimeStamps(0);
		timeStamps.setArrivalTime(arrivalTime);
		return timeStamps;
	}

	private static final class RecordingProvider implements DispatchProvider {
		private final long selectedId;
		private int callCount;
		private DispatchDecisionRequest request;

		private RecordingProvider(final long selectedId) {
			this.selectedId = selectedId;
		}

		@Override
		public DispatchDecisionResponse selectDispatchCandidate(final DispatchDecisionRequest request) {
			this.callCount++;
			this.request = request;
			return DispatchDecisionResponse.of(this.selectedId);
		}
	}

	private static final class TestFixture {
		private final FabModel model;
		private final ToolGroup toolGroup;
		private final AbstractTool tool;
		private final Lot firstItem;
		private final Lot secondItem;
		private final ArrayList<AbstractFlowItem> items;

		private TestFixture(final FabModel model, final ToolGroup toolGroup, final AbstractTool tool,
				final Lot firstItem, final Lot secondItem, final ArrayList<AbstractFlowItem> items) {
			this.model = model;
			this.toolGroup = toolGroup;
			this.tool = tool;
			this.firstItem = firstItem;
			this.secondItem = secondItem;
			this.items = items;
		}
	}
}
