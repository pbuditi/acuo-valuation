package com.acuo.valuation.protocol.results;

@lombok.Value
public class MarginValuation {

    String name;
    Double account;
    Double change;
    Double margin;
    String marginType;

}
