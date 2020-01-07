package de.terministic.fabsim.tests.batchtests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.terministic.fabsim.components.BasicRouting;
import de.terministic.fabsim.components.Lot;
import de.terministic.fabsim.components.Product;
import de.terministic.fabsim.core.FabModel;

public class LotSplitRevertTest {

	Lot lot1;
	Lot lot2;
	FabModel model = new FabModel();

	@Test
	public void SplitRevertTest() {
		this.model = new FabModel();
		Product prod = new Product("Product", null);
		this.lot1 = new Lot(this.model, prod, 6, 0, Long.MAX_VALUE);
		this.lot1.setOriginalLotSize(6);
		final long a = this.lot1.getId();
		final int b = this.lot1.getSize();
		this.lot2 = this.lot1.splitOfChild(3);
		this.lot1.mergeWithLot(this.lot2);
		final long c = this.lot1.getId();
		final int d = this.lot1.getSize();
		assertEquals(a, c);
		assertEquals(b, d);
	}

}