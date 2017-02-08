/**
  * Copyright 2017 aTool.org 
  */
package com.acuo.valuation.jackson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

@lombok.Data
public class Agreementdetails {

    private String threshold;
    @JsonProperty("minTransfer")
    private String mintransfer;
    private String rounding;
    @JsonProperty("netRequired")
    private String netrequired;
    private String rate;
    @JsonProperty("tradeCount")
    private String tradecount;


}