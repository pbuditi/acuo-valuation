package com.acuo.valuation.providers.acuo.trades;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.valuation.builders.TradeBuilder;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.acuo.ClarusValuationProcessor;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
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
        log.info("processing {} clarus trades", Iterables.size(trades));
        Collection<MarginCall> results = internal(trades);
        log.info("generated {} clarus results", results.size());
        if (next != null) {
            results.addAll(next.process(trades));
        }
        return results;
    }

    protected abstract MarginResults send(List<com.acuo.common.model.trade.Trade> swapTrades);

    private <T extends Trade> Collection<MarginCall> internal(Iterable<T> entities) {
        try {
            if (Iterables.isEmpty(entities))
                return new ArrayList<>();
            final List<com.acuo.common.model.trade.Trade> trades = StreamSupport.stream(entities.spliterator(), false)
                    .filter(predicate)
                    .map(TradeBuilder::buildTrade)
                    .collect(toList());
            if (Iterables.isEmpty(trades))
                return new ArrayList<>();
            MarginResults results = send(trades);
            return resultProcessor.process(results);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
