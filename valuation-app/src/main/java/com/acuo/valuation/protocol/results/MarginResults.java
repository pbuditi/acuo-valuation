package com.acuo.valuation.protocol.results;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.collect.result.Result;

import java.time.LocalDate;
import java.util.List;

public class MarginResults {

    private String marginType;

    private LocalDate valuationDate;

    private String portfolioId;


    public String getMarginType() {
        return marginType;
    }

    public void setMarginType(String marginType) {
        this.marginType = marginType;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    private final ImmutableList<Result<MarginValuation>> results;

    private MarginResults(List<Result<MarginValuation>> results) {
        this.results = ImmutableList.copyOf(results);
    }

    public static MarginResults of(List<Result<MarginValuation>> results) {
        return new MarginResults(results);
    }

    public ImmutableList<Result<MarginValuation>> getResults() {
        return results;
    }
}
