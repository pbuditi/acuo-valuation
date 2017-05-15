package com.acuo.valuation.protocol.results;

import com.acuo.common.model.margin.Types;
import com.opengamma.strata.collect.result.Result;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarginResults {

    private Types.CallType marginType;
    private LocalDate valuationDate;
    private String currency;
    private List<Result<MarginValuation>> results;
}
