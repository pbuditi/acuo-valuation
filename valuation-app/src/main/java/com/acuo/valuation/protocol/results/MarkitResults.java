package com.acuo.valuation.protocol.results;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarkitResults {

    private LocalDate valuationDate;
    private Currency currency;
    private List<Result<MarkitValuation>> results;

}
