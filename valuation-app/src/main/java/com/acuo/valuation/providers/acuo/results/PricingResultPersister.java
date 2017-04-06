package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.*;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.Value;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
public class PricingResultPersister implements ResultPersister<PricingResults>, MarkitValuationProcessor.PricingResultProcessor {

    private final TradeService<Trade> tradeService;
    private final ValuationService valuationService;
    private final ValueService valueService;
    private MarkitValuationProcessor.PricingResultProcessor nextProcessor;
    private final PortfolioService portfolioService;

    @Inject
    public PricingResultPersister(TradeService<Trade> tradeService, ValuationService valuationService, ValueService valueService, PortfolioService portfolioService) {
        this.tradeService = tradeService;
        this.valuationService = valuationService;
        this.valueService = valueService;
        this.portfolioService = portfolioService;
    }

    @Override
    public MarkitValuationProcessor.ProcessorItem process(MarkitValuationProcessor.ProcessorItem processorItem) {
        log.info("processing item {}", processorItem);
        PricingResults results = processorItem.getResults();
        Set<PortfolioId> portfolioIds = persist(results);
        processorItem.setPortfolioIds(portfolioIds);
        if (nextProcessor != null)
            return nextProcessor.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public void setNextProcessor(MarkitValuationProcessor.PricingResultProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    public Set<PortfolioId> persist(PricingResults pricingResults) {

        if (pricingResults == null) {
            log.warn("pricingResults is null");
            return Collections.emptySet();
        }

        log.info("persisting {} markit result of {}", pricingResults.getResults().size(), pricingResults.getDate());

        LocalDate date = pricingResults.getDate();
        Currency currency = pricingResults.getCurrency();

        List<Result<MarkitValuation>> results = pricingResults.getResults();
        Set<PortfolioId> portfolioIds = results.stream()
                .flatMap(markitValuationResult -> markitValuationResult.stream())
                .flatMap(markitValuation -> markitValuation.getValues().stream())
                .map(value -> {
                    Valuation valuation = convert(date, currency, value);
                    return valuation.getPortfolio().getPortfolioId();
                }).collect(toSet());
        return portfolioIds;
    }

    private Valuation convert(LocalDate date, Currency currency, Value value) {
        String tradeId = value.getTradeId();
        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));

        /*Set<TradeValueRelation> existedValues = valuation.getValues();
        if (existedValues != null) {
            for (TradeValueRelation existedValue : existedValues) {
                TradeValue tradeValue = existedValue.getValue();
                if (existedValue.getDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                        && tradeValue.getCurrency().equals(currency) && tradeValue.getSource().equalsIgnoreCase("Markit")) {
                    log.debug("deleting value id [{}]", tradeValue.getId());
                    valueService.delete(tradeValue.getId());
                    existedValues.remove(tradeValue);
                    break;
                }
            }
        }*/

        TradeValue newValue = createValue(currency, value.getPv(), "Markit");
        TradeValueRelation valueRelation = new TradeValueRelation();
        valueRelation.setValuation(valuation);
        valueRelation.setDateTime(date);
        valueRelation.setValue(newValue);
        newValue.setValuation(valueRelation);

        valueService.createOrUpdate(newValue);
        valuation = valuationService.getTradeValuationFor(TradeId.fromString(tradeId));
        return valuation;
    }


    private TradeValue createValue(Currency currency, Double pv, String source) {
        TradeValue newValue = new TradeValue();
        newValue.setSource(source);
        newValue.setCurrency(currency);
        newValue.setPv(pv);
        return newValue;
    }
}