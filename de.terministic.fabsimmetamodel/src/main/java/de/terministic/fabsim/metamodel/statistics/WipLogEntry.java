package de.terministic.fabsim.metamodel.statistics;

public class WipLogEntry {

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getLots() {
		return lots;
	}

	public void setLots(long lots) {
		this.lots = lots;
	}

	public long getWafer() {
		return wafer;
	}

	public void setWafer(long wafer) {
		this.wafer = wafer;
	}

	private long time;
	private long lots;
	private long wafer;
}
