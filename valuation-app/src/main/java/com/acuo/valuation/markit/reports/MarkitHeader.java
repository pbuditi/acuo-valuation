package com.acuo.valuation.markit.reports;

import java.util.Date;

public class MarkitHeader implements Header {

	private String name;
	private String version;
	private Date date;
	private Date valuationDate;
	private String valuationCurrency;
	private Integer successfulTrades;
	private Integer failedTrades;
	private Integer totalTrades;
	private Boolean valuationsComplete;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getValuationDate() {
		return valuationDate;
	}

	public void setValuationDate(Date valuationDate) {
		this.valuationDate = valuationDate;
	}

	public String getValuationCurrency() {
		return valuationCurrency;
	}

	public void setValuationCurrency(String valuationCurrency) {
		this.valuationCurrency = valuationCurrency;
	}

	public Integer getSuccessfulTrades() {
		return successfulTrades;
	}

	public void setSuccessfulTrades(Integer successfulTrades) {
		this.successfulTrades = successfulTrades;
	}

	public Integer getFailedTrades() {
		return failedTrades;
	}

	public void setFailedTrades(Integer failedTrades) {
		this.failedTrades = failedTrades;
	}

	public Integer getTotalTrades() {
		return totalTrades;
	}

	public void setTotalTrades(Integer totalTrades) {
		this.totalTrades = totalTrades;
	}

	public Boolean getValuationsComplete() {
		return valuationsComplete;
	}

	public void setValuationsComplete(Boolean valuationsComplete) {
		this.valuationsComplete = valuationsComplete;
	}

	@Override
	public String toString() {
		return "MarkitHeader [name=" + name + ", version=" + version + ", date=" + date + ", valuationDate="
				+ valuationDate + ", valuationCurrency=" + valuationCurrency + ", successfulTrades=" + successfulTrades
				+ ", failedTrades=" + failedTrades + ", totalTrades=" + totalTrades + ", valuationsComplete="
				+ valuationsComplete + "]";
	}
}