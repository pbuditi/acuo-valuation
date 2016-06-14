package com.acuo.valuation.reports;

import java.util.Date;

public interface Header {

	String getName();

	String getVersion();

	Date getDate();

	Date getValuationDate();

	String getValuationCurrency();

	Integer getSuccessfulTrades();

	Integer getFailedTrades();

	Integer getTotalTrades();

	Boolean getValuationsComplete();
}