package com.acuo.valuation.providers.acuo.trades;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Trade;

import java.util.Collection;

public interface TradePricingProcessor {

    <T extends Trade> Collection<MarginCall> process(Iterable<T> trades);

    void setNext(TradePricingProcessor next);
}
