package com.acuo.valuation.protocol.results;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.collect.result.Result;

import java.util.List;

public class PricingResults {

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
}
