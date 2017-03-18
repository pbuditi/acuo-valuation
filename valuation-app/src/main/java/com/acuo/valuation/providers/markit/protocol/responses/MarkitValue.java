package com.acuo.valuation.providers.markit.protocol.responses;

import com.acuo.valuation.protocol.results.Value;
import lombok.Data;

@Data
public class MarkitValue implements Value {

    private String tradeId;
    private String book;
    private Double pvLocal;
    private Double portfolioValuationsLocal;
    private Double pv;
    private Double accrued;
    private Double parRate;
    private String localCurrency;
    private String status;
    private String legId;
    private Double notional;
    private String valuationCcy;
    private Double pv01;
    private Double cleanPVLocal;
    private Double cleanPV;
    private String accruedValCcy;
    private Double dirtyPrice;
    private Double cleanPrice;
    private Double priceAccrued;
    private Double pv01Local;
    private String fas157Rating;
    private String instrumentCode;
    private String instrumentType;
    private String errorMessage;

}