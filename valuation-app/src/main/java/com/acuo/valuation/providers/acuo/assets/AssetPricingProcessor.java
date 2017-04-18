package com.acuo.valuation.providers.acuo.assets;

import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.AssetValue;

import java.util.Collection;

public interface AssetPricingProcessor {

    Collection<AssetValue> process(Iterable<Asset> assets);

    void setNext(AssetPricingProcessor next);
}