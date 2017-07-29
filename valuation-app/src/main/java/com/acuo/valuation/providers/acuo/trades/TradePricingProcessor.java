package com.acuo.valuation.providers.acuo.trades;

import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.PortfolioId;

import java.util.Set;

public interface TradePricingProcessor {

    <T extends Trade> Set<PortfolioId> process(Iterable<T> trades);

    void setNext(TradePricingProcessor next);
}
