package com.acuo.valuation.providers.acuo.assets;

public abstract class AbstractAssetPricingProcessor implements AssetPricingProcessor {

    protected AssetPricingProcessor next;

    @Override
    public final void setNext(AssetPricingProcessor next) {
        this.next = next;
    }
}
