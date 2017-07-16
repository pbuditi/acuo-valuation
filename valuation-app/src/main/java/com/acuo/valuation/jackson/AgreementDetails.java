package com.acuo.valuation.jackson;

import com.acuo.common.json.DoubleSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class AgreementDetails {

    private Long tradeCount;
    private Long tradeValue;
    private String pricingSource;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double threshold;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double minTransfer;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double rounding;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double netRequired;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double rate;


}