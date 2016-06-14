package com.acuo.valuation.markit.reports;

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
}