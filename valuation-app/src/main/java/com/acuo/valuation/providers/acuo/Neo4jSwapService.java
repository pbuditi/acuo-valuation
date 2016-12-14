package com.acuo.valuation.providers.acuo;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.Value;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.SwapService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class Neo4jSwapService implements SwapService {

    private final PricingService pricingService;
    private final Neo4jPersistService sessionProvider;
    private final TradeService tradeService;

    @Inject
    public Neo4jSwapService(PricingService pricingService, Neo4jPersistService sessionProvider, TradeService tradeService) {
        this.pricingService = pricingService;
        this.sessionProvider = sessionProvider;
        this.tradeService = tradeService;
    }

    @Override
    public PricingResults price(String swapId) {
        try {
            List<SwapTrade> swapTrades = getSwapTrades(swapId);
            return pricingService.price(swapTrades);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean persist(PricingResults pricingResults) {
        if (pricingResults == null) {
            log.warn("received a null pricing results to persist");
            return false;
        }

        LocalDate date = pricingResults.getDate();

        Currency currency = pricingResults.getCurrency();

        log.debug("persist start :" + pricingResults.getDate());

        ImmutableList<com.opengamma.strata.collect.result.Result<MarkitValuation>> results = pricingResults.getResults();
        for (com.opengamma.strata.collect.result.Result<MarkitValuation> result : results) {
            log.debug(result.toString());
            for (Value value : result.getValue().getValues()) {
                String tradeId = value.getTradeId();
                Double pv = value.getPv();

                log.debug("tradeId:" + tradeId);

                Trade trade = tradeService.findById(tradeId);

                log.debug(trade.toString());

                Set<Valuation> valuations = trade.getValuations();
                boolean found = false;
                if (valuations != null) {
                    for (Valuation valuation : valuations) {
                        log.debug("date in valuation : " + valuation.getDate());
                        if (valuation.getDate().equals(date)) {
                            log.debug("existing valuation");
                            //existing date, add or replace the value
                            Set<com.acuo.persist.entity.Value> existedValues = valuation.getValues();
                            for (com.acuo.persist.entity.Value existedValue : existedValues) {
                                if (existedValue.getCurrency().equals(currency) && existedValue.getSource().equalsIgnoreCase("Markit"))
                                    sessionProvider.get().delete(existedValue);
                            }

                            com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                            newValue.setSource("Markit");
                            newValue.setCurrency(currency);
                            newValue.setPv(value.getPv());

                            valuation.getValues().add(newValue);

                            sessionProvider.get().save(valuation, 2);

                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    //new valutaion

                    Valuation valuation = new Valuation();

                    valuation.setDate(date);

                    log.debug("new valuation:" + valuation.getDate());

                    com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                    newValue.setSource("Markit");
                    newValue.setCurrency(currency);
                    newValue.setPv(value.getPv());

                    Set<com.acuo.persist.entity.Value> values = new HashSet<com.acuo.persist.entity.Value>();

                    values.add(newValue);

                    valuation.setValues(values);

                    if(trade.getValuations() != null)
                        trade.getValuations().add(valuation);
                    else
                    {
                        Set<Valuation> valuationSet = new HashSet<Valuation>();
                        trade.setValuations(valuationSet);

                    }



                    sessionProvider.get().save(trade, 2);

                }
            }
        }
        return true;
    }

    private List<SwapTrade> getSwapTrades(String swapId) {
        Trade trade = tradeService.findById(swapId);
        SwapTrade swapTrade = SwapTradeBuilder.buildTrade((IRS) trade);
        log.debug("swapTrade:" + swapTrade);
        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
        swapTrades.add(swapTrade);
        return swapTrades;
    }

    public static LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}