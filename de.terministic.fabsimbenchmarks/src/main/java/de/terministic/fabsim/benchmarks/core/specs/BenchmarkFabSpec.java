package de.terministic.fabsim.benchmarks.core.specs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BenchmarkFabSpec {
	private final String sinkName;
	private final int lotSize;
	private final boolean allowSplit;
	private final List<BenchmarkToolGroupSpec> toolGroups;
	private final List<BenchmarkProductSpec> products;

	public BenchmarkFabSpec(final String sinkName, final int lotSize, final boolean allowSplit,
			final List<BenchmarkToolGroupSpec> toolGroups, final List<BenchmarkProductSpec> products) {
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

	private static List<BenchmarkToolGroupSpec> copyToolGroups(final List<BenchmarkToolGroupSpec> toolGroups) {
		if (toolGroups == null || toolGroups.isEmpty()) {
			throw new IllegalArgumentException("toolGroups must not be empty");
		}
		final ArrayList<BenchmarkToolGroupSpec> copy = new ArrayList<>(toolGroups.size());
		for (final BenchmarkToolGroupSpec toolGroup : toolGroups) {
			if (toolGroup == null) {
				throw new IllegalArgumentException("toolGroups must not contain null entries");
			}
			copy.add(toolGroup);
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<BenchmarkProductSpec> copyProducts(final List<BenchmarkProductSpec> products) {
		if (products == null || products.isEmpty()) {
			throw new IllegalArgumentException("products must not be empty");
		}
		final ArrayList<BenchmarkProductSpec> copy = new ArrayList<>(products.size());
		for (final BenchmarkProductSpec product : products) {
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

	public List<BenchmarkToolGroupSpec> getToolGroups() {
		return this.toolGroups;
	}

	public List<BenchmarkProductSpec> getProducts() {
		return this.products;
	}
}
