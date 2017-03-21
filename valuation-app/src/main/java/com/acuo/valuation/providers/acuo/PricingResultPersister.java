package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.Valuation;
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
    private final PortfolioService portfolioService;
    private final ValueService valueService;
    private MarkitValuationProcessor.PricingResultProcessor nextProcessor;

    @Inject
    public PricingResultPersister(TradeService<Trade> tradeService, ValuationService valuationService, PortfolioService portfolioService, ValueService valueService) {
        this.tradeService = tradeService;
        this.valuationService = valuationService;
        this.portfolioService = portfolioService;
        this.valueService = valueService;
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

                log.debug("tradeId:" + tradeId);

                Trade trade = tradeService.findById(tradeId);

                if (trade == null) {
                    log.warn("no trade with id {}", tradeId);
                    continue;
                }

                Set<Valuation> valuations = trade.getValuations();
                boolean found = false;
                if (valuations != null) {
                    for (Valuation valuation : valuations) {
                        if (valuation.getDate().equals(date)) {
                            //existing date, add or replace the value
                            if (valuation.getValues() == null)
                                valuation.setValues(new HashSet<>());
                            Set<com.acuo.persist.entity.Value> existedValues = valuation.getValues();

                            for (com.acuo.persist.entity.Value existedValue : existedValues) {
                                if (existedValue.getCurrency().equals(currency) && existedValue.getSource().equalsIgnoreCase("Markit")) {
                                    log.debug("deleting value id [{}]", existedValue.getId());
                                    try {
                                        valueService.delete(existedValue.getId());
                                        existedValues.remove(existedValue);
                                    } catch (Exception e) {
                                    }
                                }
                            }

                            com.acuo.persist.entity.Value newValue = createValue(currency, value.getPv(), "Markit");
                            valuation.getValues().add(newValue);
                            valuationService.createOrUpdate(valuation);
                            trade.getValuations().add(valuation);
                            portfolioIds.add(PortfolioId.fromString(trade.getPortfolio().getPortfolioId()));
                            addsumValuationOfPortfolio(trade.getPortfolio(), date, currency, "Markit", value.getPv());
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    //new valutaion

                    Valuation valuation = createValuation(date, trade.getTradeId());
                    com.acuo.persist.entity.Value newValue = createValue(currency, value.getPv(), "Markit");
                    Set<com.acuo.persist.entity.Value> values = new HashSet<>();
                    values.add(newValue);
                    valuation.setValues(values);

                    if (trade.getValuations() != null)
                        trade.getValuations().add(valuation);
                    else {
                        Set<Valuation> valuationSet = new HashSet<Valuation>();
                        trade.setValuations(valuationSet);
                    }


                    valuationService.createOrUpdate(valuation);
                    tradeService.save(trade);
                    portfolioIds.add(PortfolioId.fromString(trade.getPortfolio().getPortfolioId()));
                    addsumValuationOfPortfolio(trade.getPortfolio(), date, currency, "Markit", value.getPv());
                }
            }
        }
        return portfolioIds;
    }

    private Valuation createValuation(LocalDate date, String tradeId) {
        Valuation valuation = new Valuation();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateFormatted = date.format(formatter);
        valuation.setValuationId(dateFormatted + "-" + tradeId);
        valuation.setDate(date);
        return valuation;
    }

    private com.acuo.persist.entity.Value createValue(Currency currency, Double pv, String source) {
        com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();
        newValue.setSource(source);
        newValue.setCurrency(currency);
        newValue.setPv(pv);
        return newValue;
    }

    private void addsumValuationOfPortfolio(Portfolio portfolio, LocalDate date, com.opengamma.strata.basics.currency.Currency currency, String source, Double pv) {

        portfolio = portfolioService.findById(portfolio.getPortfolioId(), 2);

        Valuation theValuation = null;
        com.acuo.persist.entity.Value theValue = null;


        if (portfolio.getValuations() != null) {
            for (Valuation valuation : portfolio.getValuations()) {
                if (valuation.getDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))) {
                    theValuation = valuation;
                    if (valuation.getValues() != null)
                        for (com.acuo.persist.entity.Value value : valuation.getValues()) {
                            if (value.getCurrency().equals(currency) && value.getSource().equals(source))
                                theValue = value;
                        }
                }
            }
        }


        if (theValuation == null) {
            theValuation = createValuation(date, portfolio.getPortfolioId());
            theValue = createValue(currency, pv, "Markit");
            Set<com.acuo.persist.entity.Value> values = new HashSet<>();
            values.add(theValue);
            theValuation.setValues(values);

            if (portfolio.getValuations() == null)
                portfolio.setValuations(new HashSet<Valuation>());

            portfolio.getValuations().add(theValuation);
            portfolioService.createOrUpdate(portfolio);
        } else {
            if (theValue == null) {
                theValue = createValue(currency, pv, source);
                if (theValuation.getValues() == null)
                    theValuation.setValues(new HashSet<>());
                theValuation.getValues().add(theValue);
                valuationService.createOrUpdate(theValuation);
            } else {
                theValue.setPv(theValue.getPv() + pv);
                valueService.createOrUpdate(theValue);
            }
        }
    }
}
