package com.acuo.valuation.protocol.results;

@lombok.Value
public class MarginResult implements Result {

    String name;
    Double account;
    Double change;
    Double margin;

}
