package com.acuo.valuation.providers.acuo.trades;

import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.builders.TradeBuilder;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.acuo.persist.entity.enums.PricingProvider.Clarus;
import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class ClarusPricingProcessor extends AbstractTradePricingProcessor {

    private final ResultPersister<MarginResults> resultProcessor;

    private static final Predicate<Trade> predicate = trade -> {
        final PricingSource pricingSource = trade.getPricingSource();
        return Clarus.equals(pricingSource.getName());
    };

    ClarusPricingProcessor(ResultPersister<MarginResults> resultProcessor) {
        this.resultProcessor = resultProcessor;
    }

    @Override
    public <T extends Trade> Set<PortfolioId> process(Iterable<T> trades) {
        log.info("processing {} clarus trades", Iterables.size(trades));
        Set<PortfolioId> results = internal(trades);
        log.info("generated {} clarus results", results.size());
        if (next != null) {
            results.addAll(next.process(trades));
        }
        return results;
    }

    protected abstract MarginResults send(List<com.acuo.common.model.trade.Trade> swapTrades);

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
            MarginResults results = send(trades);
            return resultProcessor.persist(results);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new HashSet<>();
        }
    }
}
