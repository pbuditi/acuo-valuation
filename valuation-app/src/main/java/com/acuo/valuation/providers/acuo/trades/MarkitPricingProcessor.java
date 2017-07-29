package com.acuo.valuation.providers.acuo.trades;


import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.common.model.ids.PortfolioId;
import com.acuo.valuation.builders.TradeBuilder;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.services.PricingService;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.Markit;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MarkitPricingProcessor extends AbstractTradePricingProcessor {

    private final PricingService pricingService;
    private final ResultPersister<MarkitResults> resultProcessor;
    private static final boolean USE_BULK_PRICING = false;

    private static final Predicate<Trade> predicate = trade -> {
        final PricingSource pricingSource = trade.getPricingSource();
        return Markit.equals(pricingSource.getName());
    };

    @Inject
    public MarkitPricingProcessor(PricingService pricingService,
                                  ResultPersister<MarkitResults> resultProcessor) {
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
    }

    @Override
    public <T extends Trade> Set<PortfolioId> process(Iterable<T> trades) {
        log.info("processing {} markit trades", Iterables.size(trades));
        Set<PortfolioId> results = internal(trades);
        log.info("generated {} markit results", results.size());
        if (next != null) {
            results.addAll(next.process(trades));
        }
        return results;
    }

    private <T extends Trade> Set<PortfolioId> internal(Iterable<T> entities) {
        try {
            if (Iterables.isEmpty(entities))
                return new HashSet<>();
            final List<com.acuo.common.model.trade.Trade> trades = StreamSupport.stream(entities.spliterator(), false)
                    .filter(predicate)
                    .map(TradeBuilder::buildTrade)
                    .collect(toList());
            if (Iterables.isEmpty(trades))
                return new HashSet<>();
            MarkitResults results = (USE_BULK_PRICING) ?
                    pricingService.priceSwapTradesByBulk(trades) :
                    pricingService.priceSwapTrades(trades);
            return resultProcessor.persist(results);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new HashSet<>();
        }
    }
}
