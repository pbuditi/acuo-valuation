package com.acuo.valuation.jackson;

import com.acuo.common.json.DoubleSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@lombok.Data
public class Agreementdetails {
    @JsonSerialize(using = DoubleSerializer.class)
    private Double threshold;
    @JsonProperty("minTransfer")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double mintransfer;
    @JsonSerialize(using = DoubleSerializer.class)
    private Double rounding;
    @JsonProperty("netRequired")
    @JsonSerialize(using = DoubleSerializer.class)
    private Double netrequired;
    @JsonSerialize(using = DoubleSerializer.class)
    private Double rate;
    @JsonProperty("tradeCount")
    private String tradecount;


}