package com.acuo.valuation.providers.acuo.trades;

public abstract class AbstractTradePricingProcessor implements TradePricingProcessor {

    protected TradePricingProcessor next;

    @Override
    public final void setNext(TradePricingProcessor next) {
        this.next = next;
    }
}
