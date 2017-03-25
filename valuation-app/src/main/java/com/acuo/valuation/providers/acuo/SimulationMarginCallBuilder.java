package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.*;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.*;
import com.acuo.valuation.services.MarginCallGenService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class SimulationMarginCallBuilder extends MarginCallGenerator implements MarginCallGenService, MarkitValuationProcessor.PricingResultProcessor {

    private MarkitValuationProcessor.PricingResultProcessor nextProcessor;

    @Inject
    public SimulationMarginCallBuilder(ValuationService valuationService,
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
        processorItem.setSimulated(marginCalls);
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
        List<MarginCall> marginCalls = new ArrayList<MarginCall>();
        double startVar = 1;
        int i = 0;
        for (PortfolioId portfolioId : portfolioSet) {
            //for the random var
            int index = (i + 1) / 2;
            double rate = 0.2;
            double var;
            if (i % 2 == 0)
                var = startVar + rate * index;
            else
                var = startVar - rate * index;

            Portfolio portfolio = portfolioService.findById(portfolioId.toString());

            Valuation valuation = valuationService.findById(date.format(dateTimeFormatter) + "-" + portfolio.getPortfolioId());
            generateMarginCall((TradeValuation) valuation, date, CallStatus.Unrecon);

        }
        return marginCalls;
    }
}
