package de.terministic.fabsim.metamodel.components.equipment;

public class BatchDetails {
	private String batchId;
	private int maxBatch;
	private long maxWait;
	private int minBatch;

	public BatchDetails(final String batchId, final int minBatch, final int maxBatch) {
		valueSanityCheck(minBatch, maxBatch, Long.MAX_VALUE);
		this.batchId = batchId;
		this.minBatch = minBatch;
		this.maxBatch = maxBatch;
		this.maxWait = Long.MAX_VALUE;
	}

	public BatchDetails(final String batchId, final int minBatch, final int maxBatch, final long maxWait) {
		valueSanityCheck(minBatch, maxBatch, maxWait);
		this.batchId = batchId;
		this.minBatch = minBatch;
		this.maxBatch = maxBatch;
		this.maxWait = maxWait;
	}

	public String getBatchId() {
		return this.batchId;
	}

	public int getMaxBatch() {
		return this.maxBatch;
	}

	public long getMaxWait() {
		return this.maxWait;
	}

	public int getMinBatch() {
		return this.minBatch;
	}

	public void setBatchId(final String batchId) {
		this.batchId = batchId;
	}

	public void setMaxBatch(final int maxBatch) {
		valueSanityCheck(this.minBatch, maxBatch, this.maxWait);
		this.maxBatch = maxBatch;
	}

	public void setMaxWait(final long maxWait) {
		valueSanityCheck(this.minBatch, this.maxBatch, maxWait);
		this.maxWait = maxWait;
	}

	public void setMinBatch(final int minBatch) {
		valueSanityCheck(minBatch, this.maxBatch, this.maxWait);
		this.minBatch = minBatch;
	}

	private void valueSanityCheck(final int minBatch, final int maxBatch, final long maxWait) {
		if (minBatch > maxBatch)
			throw new IllegalArgumentException("minBatch must be smaller or equal to maxBatch");
		if (minBatch <= 0)
			throw new IllegalArgumentException("minBatch must be greater than 0");
		if (maxWait < 0)
			throw new IllegalArgumentException("maxWait must not be negative");

	}

	@Override
	public String toString() {
		return "BatchDetails(" + minBatch + "," + maxBatch + "," + maxWait + ")";
	}

}
