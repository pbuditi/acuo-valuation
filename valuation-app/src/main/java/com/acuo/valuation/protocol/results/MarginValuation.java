package com.acuo.valuation.protocol.results;

import com.acuo.common.model.margin.Types;

@lombok.Value
public class MarginValuation {

    String name;
    Double account;
    Double change;
    Double margin;
    Types.CallType marginType;
    String portfolioId;

}
