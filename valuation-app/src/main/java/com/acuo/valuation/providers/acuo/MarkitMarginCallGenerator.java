package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.*;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.*;
import com.acuo.valuation.services.MarginCallGenService;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.helpers.collection.Iterators;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class MarkitMarginCallGenerator extends MarginCallGenerator implements MarginCallGenService, MarkitValuationProcessor.PricingResultProcessor {

    private MarkitValuationProcessor.PricingResultProcessor nextProcessor;

    @Inject
    public MarkitMarginCallGenerator(ValuationService valuationService,
                                     PortfolioService portfolioService,
                                     MarginStatementService marginStatementService,
                                     AgreementService agreementService,
                                     CurrencyService currencyService) {
        super(valuationService,
                portfolioService,
                marginStatementService,
                agreementService,
                currencyService);
    }

    @Override
    public MarkitValuationProcessor.ProcessorItem process(MarkitValuationProcessor.ProcessorItem processorItem) {
        log.info("processing item {}", processorItem);
        LocalDate date = processorItem.getResults().getDate();
        Set<PortfolioId> portfolioIds = processorItem.getPortfolioIds();
        List<MarginCall> marginCalls = marginCalls(portfolioIds, date);
        processorItem.setExpected(marginCalls);
        if (nextProcessor!= null)
            return nextProcessor.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public void setNextProcessor(MarkitValuationProcessor.PricingResultProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    public List<MarginCall> marginCalls(Set<PortfolioId> portfolioSet, LocalDate date) {
        log.info("generating margin calls for {}", portfolioSet);
        List<MarginCall> marginCalls = portfolioSet.stream()
                .map(portfolioId -> portfolioService.findById(portfolioId.toString(), 2))
                .flatMap(portfolio -> portfolio.getValuations().stream())
                .filter(valuation -> valuation.getDate().equals(date))
                .map(valuation -> generateMarginCall((TradeValuation)valuation, CallStatus.Expected))
                .collect(toList());
        log.info("{} margin calls generated", marginCalls.size());
        return marginCalls;
    }
}