package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.entity.TradeValueRelation;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class MarkitResultPersister implements ResultPersister<MarkitResults>, MarkitResultProcessor {

    private final TradeService<Trade> tradeService;
    private final ValuationService valuationService;
    private final ValueService valueService;
    private MarkitResultProcessor nextProcessor;
    private final PortfolioService portfolioService;

    @Inject
    public MarkitResultPersister(TradeService<Trade> tradeService, ValuationService valuationService, ValueService valueService, PortfolioService portfolioService) {
        this.tradeService = tradeService;
        this.valuationService = valuationService;
        this.valueService = valueService;
        this.portfolioService = portfolioService;
    }

    @Override
    public MarkitValuationProcessor.ProcessorItem process(MarkitValuationProcessor.ProcessorItem processorItem) {
        log.info("processing markit valuation items");
        MarkitResults results = processorItem.getResults();
        Set<PortfolioId> portfolioIds = persist(results);
        processorItem.setPortfolioIds(portfolioIds);
        if (nextProcessor != null)
            return nextProcessor.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public void setNext(MarkitResultProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    public Set<PortfolioId> persist(MarkitResults markitResults) {

        if (markitResults == null) {
            log.warn("markitResults is null");
            return Collections.emptySet();
        }

        log.info("persisting {} markit result of {}", markitResults.getResults().size(), markitResults.getDate());

        LocalDate date = markitResults.getDate();
        Currency currency = markitResults.getCurrency();

        List<Result<MarkitValuation>> results = markitResults.getResults();
        List<TradeValue> values = results.stream()
                .flatMap(markitValuationResult -> markitValuationResult.stream())
                .map(value -> convert(date, currency, value))
                .collect(toList());
        valueService.save(values);
        Set<PortfolioId> portfolioIds = values.stream()
                .map(value -> value.getValuation().getValuation().getPortfolio().getPortfolioId())
                .collect(toSet());
        return portfolioIds;
    }

    private TradeValue convert(LocalDate date, Currency currency, MarkitValuation value) {
        String tradeId = value.getTradeId();
        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));

        TradeValue newValue = createValue(currency, value.getPv(), "Markit");
        TradeValueRelation valueRelation = new TradeValueRelation();
        valueRelation.setValuation(valuation);
        valueRelation.setDateTime(date);
        valueRelation.setValue(newValue);
        newValue.setValuation(valueRelation);


        return newValue;
    }


    private TradeValue createValue(Currency currency, Double pv, String source) {
        TradeValue newValue = new TradeValue();
        newValue.setSource(source);
        newValue.setCurrency(currency);
        newValue.setPv(pv);
        return newValue;
    }
}