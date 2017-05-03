package com.acuo.valuation.providers.acuo.trades;


import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.Markit;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MarkitPricingProcessor extends AbstractTradePricingProcessor {

    private final PricingService pricingService;
    private final MarkitValuationProcessor resultProcessor;

    private static final Predicate<Trade> predicate = trade -> {
        final PricingSource pricingSource = trade.getPricingSource();
        return Markit.equals(pricingSource.getName());
    };

    @Inject
    public MarkitPricingProcessor(PricingService pricingService, MarkitValuationProcessor resultProcessor) {
        this.pricingService = pricingService;
        this.resultProcessor = resultProcessor;
    }

    @Override
    public <T extends Trade> Collection<VariationMargin> process(Iterable<T> trades) {
        log.info("processing {} trades", Iterables.size(trades));
        Collection<VariationMargin> results = internal(trades);
        log.info("generated {} results", results.size());
        if (next != null) {
            results.addAll(next.process(trades));
        }
        return results;
    }

    private <T extends Trade> Collection<VariationMargin> internal(Iterable<T> trades) {
        if (Iterables.isEmpty(trades))
            return Collections.emptyList();
        final List<SwapTrade> swapTrades = StreamSupport.stream(trades.spliterator(), false)
                .filter(predicate)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .map(SwapTradeBuilder::buildTrade)
                .collect(toList());
        if (Iterables.isEmpty(swapTrades))
            return Collections.emptyList();
        MarkitResults results = pricingService.priceSwapTrades(swapTrades);
        return resultProcessor.process(results);
    }
}
