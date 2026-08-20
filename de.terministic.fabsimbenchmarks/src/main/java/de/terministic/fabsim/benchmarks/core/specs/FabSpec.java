package de.terministic.fabsim.benchmarks.core.specs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FabSpec {
	private final String sinkName;
	private final int lotSize;
	private final boolean allowSplit;
	private final List<ToolGroupSpec> toolGroups;
	private final List<ProductSpec> products;

	public FabSpec(final String sinkName, final int lotSize, final boolean allowSplit,
			final List<ToolGroupSpec> toolGroups, final List<ProductSpec> products) {
		if (sinkName == null || sinkName.trim().isEmpty()) {
			throw new IllegalArgumentException("sinkName must not be blank");
		}
		if (lotSize <= 0) {
			throw new IllegalArgumentException("lotSize must be greater than 0");
		}
		this.sinkName = sinkName;
		this.lotSize = lotSize;
		this.allowSplit = allowSplit;
		this.toolGroups = copyToolGroups(toolGroups);
		this.products = copyProducts(products);
	}

	private static List<ToolGroupSpec> copyToolGroups(final List<ToolGroupSpec> toolGroups) {
		if (toolGroups == null || toolGroups.isEmpty()) {
			throw new IllegalArgumentException("toolGroups must not be empty");
		}
		final ArrayList<ToolGroupSpec> copy = new ArrayList<>(toolGroups.size());
		for (final ToolGroupSpec toolGroup : toolGroups) {
			if (toolGroup == null) {
				throw new IllegalArgumentException("toolGroups must not contain null entries");
			}
			copy.add(toolGroup);
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<ProductSpec> copyProducts(final List<ProductSpec> products) {
		if (products == null || products.isEmpty()) {
			throw new IllegalArgumentException("products must not be empty");
		}
		final ArrayList<ProductSpec> copy = new ArrayList<>(products.size());
		for (final ProductSpec product : products) {
			if (product == null) {
				throw new IllegalArgumentException("products must not contain null entries");
			}
			copy.add(product);
		}
		return Collections.unmodifiableList(copy);
	}

	public String getSinkName() {
		return this.sinkName;
	}

	public int getLotSize() {
		return this.lotSize;
	}

	public boolean isAllowSplit() {
		return this.allowSplit;
	}

	public List<ToolGroupSpec> getToolGroups() {
		return this.toolGroups;
	}

	public List<ProductSpec> getProducts() {
		return this.products;
	}
}
