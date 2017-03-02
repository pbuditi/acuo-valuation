package com.acuo.valuation.protocol.results;

import com.opengamma.strata.collect.result.Result;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarginResults {

    private String marginType;
    private LocalDate valuationDate;
    private String portfolioId;
    private String currency;
    private List<Result<MarginValuation>> results;

}
