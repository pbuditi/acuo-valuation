package com.acuo.valuation.protocol.results;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;

import java.time.LocalDate;
import java.util.List;

public class PricingResults {

    private LocalDate date;

    private Currency currency;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
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
