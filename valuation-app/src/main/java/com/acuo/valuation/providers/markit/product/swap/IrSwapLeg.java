package com.acuo.valuation.providers.markit.product.swap;

import lombok.Setter;

@Setter
public class IrSwapLeg {

    private int id;
    private String currency;
    private IrSwapLegFixing fixing;
    private Double spread;
    private Double rate;
    private String type;
    private String daycount;
    private Double notional;
    private String notionalxg;
    private IrSwapLegPayDates paydates;

    public IrSwapLeg() {
    }

    public IrSwapLeg(IrSwapLegInput input) {
        id = input.id;
        currency = input.currency;
        fixing = input.fixing != null ? new IrSwapLegFixing(input.fixing) : null;
        spread = input.spread;
        rate = input.rate;
        type = input.type;
        daycount = input.daycount;
        notional = input.notional;
        notionalxg = input.notionalxg;
        paydates = new IrSwapLegPayDates(input.payDates);
    }

    public int id() {
        return id;
    }

    public String currency() {
        return currency;
    }

    public IrSwapLegFixing fixing() {
        return fixing;
    }

    public Double spread() {
        return spread;
    }

    public Double rate() {
        return rate;
    }

    public String type() {
        return type;
    }

    public String daycount() {
        return daycount;
    }

    public Double notional() {
        return notional;
    }

    public String notionalxg() {
        return notionalxg;
    }

    public IrSwapLegPayDates payDates() {
        return paydates;
    }
}