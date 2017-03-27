package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.*;
import com.acuo.persist.ids.PortfolioId;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        if (nextProcessor!= null)
            return nextProcessor.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public void setNextProcessor(MarkitValuationProcessor.PricingResultProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    public Set<PortfolioId> persist(PricingResults pricingResults) {
        Set<PortfolioId> portfolioIds = new TreeSet<>();

        if (pricingResults == null) {
            log.warn("pricingResults is null");
            return portfolioIds;
        }

        log.info("persisting {} markit result of {}", pricingResults.getResults().size(), pricingResults.getDate());

        LocalDate date = pricingResults.getDate();
        Currency currency = pricingResults.getCurrency();

        List<Result<MarkitValuation>> results = pricingResults.getResults();

        for (Result<MarkitValuation> result : results) {

            for (Value value : result.getValue().getValues()) {
                String tradeId = value.getTradeId();
                Trade trade = tradeService.findById(tradeId);

                if (trade == null) {
                    log.warn("no trade with id {}", tradeId);
                    continue;
                }

                Valuation valuation = trade.getValuation();

                if(valuation == null)
                {
                    TradeValuation tradeValuation = createValuation(date, trade.getTradeId());
                    tradeValuation.setTrade(trade);
                    //tradeValuation.setPortfolio(trade.getPortfolio());

                    valuationService.createOrUpdate(tradeValuation);
                    valuation = tradeValuation;
                }
                else
                {
                    valuation = valuationService.find(valuation.getId());
                }

                if(valuation.getValues() == null)
                    valuation.setValues(new HashSet<>());

                double existedPv = 0;


                Set<com.acuo.persist.entity.Value> existedValues = valuation.getValues();
                for(com.acuo.persist.entity.Value existedValue: existedValues)
                {
                    TradeValue tradeValue = (TradeValue)existedValue;
                    if(tradeValue.getDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                            && tradeValue.getCurrency().equals(currency) && tradeValue.getSource().equalsIgnoreCase("Markit"))
                    {
                        log.debug("deleting value id [{}]", tradeValue.getId());
                        existedPv = tradeValue.getPv();
                        valueService.delete(tradeValue.getId());
                        //existedValues.remove(tradeValue);
                        break;

                    }
                }

                TradeValue newValue = createValue(currency, value.getPv(), "Markit", date);
                newValue.setValuation(valuation);
                valueService.createOrUpdate(newValue);
                addsumValuationOfPortfolio(trade.getPortfolio(), date, currency, "Markit", newValue.getPv(), existedPv);
                portfolioIds.add(PortfolioId.fromString(trade.getPortfolio().getPortfolioId()));



            }
        }
        return portfolioIds;
    }

    private TradeValuation createValuation(LocalDate date, String tradeId) {
        TradeValuation valuation = new TradeValuation();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateFormatted = date.format(formatter);
        valuation.setValuationId(dateFormatted + "-" + tradeId);
        //valuation.setDate(date);
        return valuation;
    }

    private TradeValue createValue(Currency currency, Double pv, String source, LocalDate date) {
        TradeValue newValue = new TradeValue();
        newValue.setSource(source);
        newValue.setCurrency(currency);
        newValue.setPv(pv);
        newValue.setDate(date);
        return newValue;
    }

    private void addsumValuationOfPortfolio(Portfolio portfolio, LocalDate date, com.opengamma.strata.basics.currency.Currency currency, String source, Double newPv, Double existedPv)
    {

        portfolio = portfolioService.findById(portfolio.getPortfolioId(), 2);

        TradeValuation theValuation = null;
        TradeValue theValue = null;


        if(portfolio.getValuation() == null)
        {
            theValuation = new TradeValuation();
            theValuation.setValuationId(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "-" + portfolio.getPortfolioId());
            theValuation.setPortfolio(portfolio);
            valuationService.createOrUpdate(theValuation);
        }
        else
        {
            theValuation = (TradeValuation) portfolio.getValuation();
        }

        if(theValuation.getValues() == null)
            theValuation.setValues(new HashSet<>());

        Set<com.acuo.persist.entity.Value> values = theValuation.getValues();

        for(com.acuo.persist.entity.Value value : values)
        {
            TradeValue tradeValue = (TradeValue)value;
            if(tradeValue.getDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                    && tradeValue.getCurrency().equals(currency) && tradeValue.getSource().equals(source))
            {
                theValue = tradeValue;
            }
        }




        if(theValue == null)
        {
            theValue = new TradeValue();
            theValue.setPv(newPv);
            theValue.setCurrency(currency);
            theValue.setSource(source);
            theValue.setValuation(theValuation);
            theValue.setDate(date);

        }
        else
        {
            theValue.setPv(theValue.getPv() - existedPv + newPv);
        }
        valueService.createOrUpdate(theValue);

    }
}