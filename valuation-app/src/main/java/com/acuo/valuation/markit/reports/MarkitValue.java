package com.acuo.valuation.markit.reports;

public class MarkitValue implements Value {

	private String tradeId;
	private String book;
	private String pvLocal;
	private String pv;
	private String accrued;

	public String getTradeId() {
		return tradeId;
	}

	public void setTradeId(String tradeId) {
		this.tradeId = tradeId;
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public String getPvLocal() {
		return pvLocal;
	}

	public void setPvLocal(String pvLocal) {
		this.pvLocal = pvLocal;
	}

	public String getPv() {
		return pv;
	}

	public void setPv(String pv) {
		this.pv = pv;
	}

	public String getAccrued() {
		return accrued;
	}

	public void setAccrued(String accrued) {
		this.accrued = accrued;
	}

	@Override
	public String toString() {
		return "MarkitValue [tradeId=" + tradeId + ", pv=" + pv + "]";
	}

}
