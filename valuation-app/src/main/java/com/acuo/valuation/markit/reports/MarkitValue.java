package com.acuo.valuation.markit.reports;

public class MarkitValue implements Value {

	private String tradeId;
	private String book;
	private Double pvLocal;
	private Double pv;
	private Double accrued;
	private Double parRate;
	private String localCurrency;
	private String status;
	private String legId;
	private Double notional;

	@Override
	public String getTradeId() {
		return tradeId;
	}

	public void setTradeId(String tradeId) {
		this.tradeId = tradeId;
	}

	@Override
	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	@Override
	public Double getPvLocal() {
		return pvLocal;
	}

	public void setPvLocal(Double pvLocal) {
		this.pvLocal = pvLocal;
	}

	@Override
	public Double getPv() {
		return pv;
	}

	public void setPv(Double pv) {
		this.pv = pv;
	}

	@Override
	public Double getAccrued() {
		return accrued;
	}

	public void setAccrued(Double accrued) {
		this.accrued = accrued;
	}

	@Override
	public Double getParRate() {
		return parRate;
	}

	public void setParRate(Double parRate) {
		this.parRate = parRate;
	}

	@Override
	public String getLocalCurrency() {
		return localCurrency;
	}

	public void setLocalCurrency(String localCurrency) {
		this.localCurrency = localCurrency;
	}

	@Override
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getLegId() {
		return legId;
	}

	public void setLegId(String legId) {
		this.legId = legId;
	}

	@Override
	public Double getNotional() {
		return notional;
	}

	public void setNotional(Double notional) {
		this.notional = notional;
	}

	@Override
	public String toString() {
		return "MarkitValue [tradeId=" + tradeId + ", pv=" + pv + "]";
	}
}