package com.acuo.valuation.providers.markit.product.swap;

import lombok.Setter;

@Setter
class IrSwapLegFixing {

    private String name;
    private String term;
    private boolean arrears;

    public IrSwapLegFixing() {
    }

    public IrSwapLegFixing(IrSwapLegFixingInput input) {
        name = input.name;
        term = input.term;
        arrears = input.arrears;
    }

    public String name() {
        return name;
    }

    public String term() {
        return term;
    }

    public boolean isArrears() {
        return arrears;
    }
}