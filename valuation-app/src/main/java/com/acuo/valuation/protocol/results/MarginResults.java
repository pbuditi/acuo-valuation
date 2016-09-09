package com.acuo.valuation.protocol.results;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.collect.result.Result;

import java.util.List;

public class MarginResults {

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
