package com.acuo.valuation.providers.markit.product.swap;

import lombok.Setter;

import java.time.LocalDate;

@Setter
public class IrSwapLegPayDates {

    private LocalDate startDate;
    private String frequency;
    private LocalDate enddate;
    private String rollCode;
    private boolean adjust;
    private boolean eom;

    public IrSwapLegPayDates() {
    }

    public IrSwapLegPayDates(IrSwapLegPayDatesInput input) {
        startDate = input.startDate;
        frequency = input.frequency;
        enddate = input.enddate;
        rollCode = input.rollCode;
        adjust = input.adjust;
        eom = input.eom;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public String frequency() {
        return frequency;
    }

    public LocalDate enddate() {
        return enddate;
    }

    public String rollCode() {
        return rollCode;
    }

    public boolean isAdjust() {
        return adjust;
    }

    public boolean isEom() {
        return eom;
    }
}