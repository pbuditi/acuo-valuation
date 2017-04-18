package com.acuo.valuation.providers.acuo.assets;

import com.acuo.persist.entity.Asset;

public interface AssetPricingProcessor {

    void process(Iterable<Asset> assets);

    void setNext(AssetPricingProcessor next);
}