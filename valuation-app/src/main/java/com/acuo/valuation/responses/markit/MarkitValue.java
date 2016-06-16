package com.acuo.valuation.responses.markit;

import com.acuo.valuation.responses.Value;

public class MarkitValue implements Value {

	private String tradeId;
	private String book;
	private Double pvLocal;
	private Double portfolioValuationsLocal;
	private Double pv;
	private Double accrued;
	private Double parRate;
	private String localCurrency;
	private String status;
	private String legId;
	private Double notional;
	private String valuationCcy;
	private Double pv01;
	private Double cleanPVLocal;
	private Double cleanPV;
	private String accruedValCcy;
	private Double dirtyPrice;
	private Double cleanPrice;
	private Double priceAccrued;
	private Double pv01Local;
	private String fas157Rating;
	private String instrumentCode;
	private String instrumentType;
	private String errorMessage;

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
	public Double getPortfolioValuationsLocal() {
		return portfolioValuationsLocal;
	}

	public void setPortfolioValuationsLocal(Double portfolioValuationsLocal) {
		this.portfolioValuationsLocal = portfolioValuationsLocal;
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
	public String getValuationCcy() {
		return valuationCcy;
	}

	public void setValuationCcy(String valuationCcy) {
		this.valuationCcy = valuationCcy;
	}

	@Override
	public Double getPv01() {
		return pv01;
	}

	public void setPv01(Double pv01) {
		this.pv01 = pv01;
	}

	@Override
	public Double getCleanPVLocal() {
		return cleanPVLocal;
	}

	public void setCleanPVLocal(Double cleanPVLocal) {
		this.cleanPVLocal = cleanPVLocal;
	}

	@Override
	public Double getCleanPV() {
		return cleanPV;
	}

	public void setCleanPV(Double cleanPV) {
		this.cleanPV = cleanPV;
	}

	@Override
	public String getAccruedValCcy() {
		return accruedValCcy;
	}

	public void setAccruedValCcy(String accruedValCcy) {
		this.accruedValCcy = accruedValCcy;
	}

	@Override
	public Double getDirtyPrice() {
		return dirtyPrice;
	}

	public void setDirtyPrice(Double dirtyPrice) {
		this.dirtyPrice = dirtyPrice;
	}

	@Override
	public Double getCleanPrice() {
		return cleanPrice;
	}

	public void setCleanPrice(Double cleanPrice) {
		this.cleanPrice = cleanPrice;
	}

	@Override
	public Double getPriceAccrued() {
		return priceAccrued;
	}

	public void setPriceAccrued(Double priceAccrued) {
		this.priceAccrued = priceAccrued;
	}

	@Override
	public Double getPv01Local() {
		return pv01Local;
	}

	public void setPv01Local(Double pv01Local) {
		this.pv01Local = pv01Local;
	}

	@Override
	public String getFas157Rating() {
		return fas157Rating;
	}

	public void setFas157Rating(String fas157Rating) {
		this.fas157Rating = fas157Rating;
	}

	@Override
	public String getInstrumentCode() {
		return instrumentCode;
	}

	public void setInstrumentCode(String instrumentCode) {
		this.instrumentCode = instrumentCode;
	}

	@Override
	public String getInstrumentType() {
		return instrumentType;
	}

	public void setInstrumentType(String instrumentType) {
		this.instrumentType = instrumentType;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "MarkitValue [tradeId=" + tradeId + ", pv=" + pv + "]";
	}
}