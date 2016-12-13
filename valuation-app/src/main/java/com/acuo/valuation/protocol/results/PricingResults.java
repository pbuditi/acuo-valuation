package com.acuo.valuation.protocol.results;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.collect.result.Result;

import java.util.Date;
import java.util.List;

public class PricingResults {

    private Date date;

    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private final ImmutableList<Result<MarkitValuation>> results;

    private PricingResults(List<Result<MarkitValuation>> results) {
        this.results = ImmutableList.copyOf(results);
    }

    public static PricingResults of(List<Result<MarkitValuation>> results) {
        return new PricingResults(results);
    }

    public ImmutableList<Result<MarkitValuation>> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "PricingResults{" +
                "date=" + date +
                ", currency='" + currency + '\'' +
                ", results=" + results +
                '}';
    }
}
