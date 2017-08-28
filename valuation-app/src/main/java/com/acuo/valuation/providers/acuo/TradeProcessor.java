package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.Trade;
import com.acuo.common.model.ids.PortfolioId;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

@Slf4j
@Singleton
public class TradeProcessor {

    private final TradePricingProcessor pricingProcessor;
    private final PortfolioProcessor portfolioProcessor;

    @Inject
    public TradeProcessor(TradePricingProcessor pricingProcessor,
                          PortfolioProcessor portfolioProcessor) {
        this.pricingProcessor = pricingProcessor;
        this.portfolioProcessor = portfolioProcessor;
    }

    public <T extends Trade> List<MarginCall> process(Iterable<T> trades) {
        log.info("starting processing {} trade(s)", Iterables.size(trades));
        Set<PortfolioId> portfolioIds = pricingProcessor.process(trades);
        return portfolioProcessor.process(portfolioIds);
    }
}
