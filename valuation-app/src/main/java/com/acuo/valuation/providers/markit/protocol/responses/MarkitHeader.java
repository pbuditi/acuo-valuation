package com.acuo.valuation.providers.markit.protocol.responses;

import com.acuo.valuation.protocol.responses.Header;

import java.time.LocalDate;

public class MarkitHeader implements Header {

    private String name;
    private String version;
    private LocalDate date;
    private LocalDate valuationDate;
    private String valuationCurrency;
    private Integer successfulTrades;
    private Integer failedTrades;
    private Integer totalTrades;
    private Boolean valuationsComplete;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    @Override
    public String getValuationCurrency() {
        return valuationCurrency;
    }

    public void setValuationCurrency(String valuationCurrency) {
        this.valuationCurrency = valuationCurrency;
    }

    @Override
    public Integer getSuccessfulTrades() {
        return successfulTrades;
    }

    public void setSuccessfulTrades(Integer successfulTrades) {
        this.successfulTrades = successfulTrades;
    }

    @Override
    public Integer getFailedTrades() {
        return failedTrades;
    }

    public void setFailedTrades(Integer failedTrades) {
        this.failedTrades = failedTrades;
    }

    @Override
    public Integer getTotalTrades() {
        return totalTrades;
    }

    public void setTotalTrades(Integer totalTrades) {
        this.totalTrades = totalTrades;
    }

    @Override
    public Boolean getValuationsComplete() {
        return valuationsComplete;
    }

    public void setValuationsComplete(Boolean valuationsComplete) {
        this.valuationsComplete = valuationsComplete;
    }

    @Override
    public String toString() {
        return "MarkitHeader [name=" + name + ", version=" + version + ", date=" + date + ", valuationDate="
                + valuationDate + ", valuationcurrency=" + valuationCurrency + ", successfulTrades=" + successfulTrades
                + ", failedTrades=" + failedTrades + ", totalTrades=" + totalTrades + ", valuationsComplete="
                + valuationsComplete + "]";
    }
}