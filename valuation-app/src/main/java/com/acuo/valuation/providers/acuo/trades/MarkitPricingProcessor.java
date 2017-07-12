package com.acuo.valuation.providers.acuo.trades;


import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.valuation.builders.TradeBuilder;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.Markit;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MarkitPricingProcessor extends AbstractTradePricingProcessor {

    private final PricingService pricingService;
    private final MarkitValuationProcessor resultProcessor;
    private static final boolean USE_BULK_PRICING = false;

    private static final Predicate<Trade> predicate = trade -> {
        final PricingSource pricingSource = trade.getPricingSource();
        return Markit.equals(pricingSource.getName());
    };

    @Inject
    public MarkitPricingProcessor(PricingService pricingService,
                                  MarkitValuationProcessor resultProcessor) {
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
    }

    @Override
    public <T extends Trade> Collection<MarginCall> process(Iterable<T> trades) {
        log.info("processing {} trades", Iterables.size(trades));
        Collection<MarginCall> results = internal(trades);
        log.info("generated {} results", results.size());
        if (next != null) {
            results.addAll(next.process(trades));
        }
        return results;
    }

    private <T extends Trade> Collection<MarginCall> internal(Iterable<T> entities) {
        if (Iterables.isEmpty(entities))
            return new ArrayList<>();
        final List<com.acuo.common.model.trade.Trade> trades = StreamSupport.stream(entities.spliterator(), false)
                .filter(predicate)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .map(TradeBuilder::buildTrade)
                .collect(toList());
        if (Iterables.isEmpty(trades))
            return new ArrayList<>();
        MarkitResults results = (USE_BULK_PRICING) ?
                pricingService.priceSwapTradesByBulk(trades) :
                pricingService.priceSwapTrades(trades);
        return resultProcessor.process(results);
    }
}
