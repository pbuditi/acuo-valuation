package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.acuo.results.ClarusValuationProcessor;
import com.acuo.valuation.utils.SwapTradeBuilder;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.Clarus;
import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class ClarusPricingProcessor extends AbstractTradePricingProcessor {

    private final ClarusValuationProcessor resultProcessor;

    private static final Predicate<Trade> predicate = trade -> {
        final PricingSource pricingSource = trade.getPricingSource();
        return Clarus.equals(pricingSource.getName());
    };

    ClarusPricingProcessor(ClarusValuationProcessor resultProcessor) {
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

    protected abstract MarginResults send(List<SwapTrade> swapTrades);

    private <T extends Trade> Collection<MarginCall> internal(Iterable<T> trades) {
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
        MarginResults results = send(swapTrades);
        return resultProcessor.process(results);
    }
}
