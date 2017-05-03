package com.acuo.valuation.providers.acuo.trades;

import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.VariationMargin;

import java.util.Collection;

public interface TradePricingProcessor {

    <T extends Trade> Collection<VariationMargin> process(Iterable<T> trades);

    void setNext(TradePricingProcessor next);
}
