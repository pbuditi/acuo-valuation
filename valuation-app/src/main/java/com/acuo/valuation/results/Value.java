package com.acuo.valuation.results;

public interface Value {

	String getTradeId();

	String getBook();

	Double getPvLocal();

	Double getPv();

	Double getAccrued();

	Double getParRate();

	String getLocalCurrency();

	String getStatus();

	String getLegId();

	Double getNotional();

	String getErrorMessage();

	String getInstrumentType();

	String getValuationCcy();

	Double getPv01();

	Double getCleanPVLocal();

	Double getCleanPV();

	String getAccruedValCcy();

	Double getDirtyPrice();

	Double getCleanPrice();

	Double getPriceAccrued();

	Double getPv01Local();

	String getFas157Rating();

	String getInstrumentCode();

	Double getPortfolioValuationsLocal();
}