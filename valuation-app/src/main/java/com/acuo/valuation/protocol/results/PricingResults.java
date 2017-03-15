package com.acuo.valuation.protocol.results;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PricingResults {

    private LocalDate date;
    private Currency currency;
    private List<Result<MarkitValuation>> results;

}
