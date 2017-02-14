/**
  * Copyright 2017 aTool.org 
  */
package com.acuo.valuation.jackson;
import com.acuo.common.json.DoubleSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;

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