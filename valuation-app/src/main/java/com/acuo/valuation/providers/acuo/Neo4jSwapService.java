package com.acuo.valuation.providers.acuo;

import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.ProductType;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.Valuation;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.Value;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.SwapService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import com.opengamma.strata.basics.currency.*;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.model.Result;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
public class Neo4jSwapService implements SwapService {

    private final PricingService pricingService;
    private final Neo4jPersistService sessionProvider;

    @Inject
    public Neo4jSwapService(PricingService pricingService, Neo4jPersistService sessionProvider) {
        this.pricingService = pricingService;
        this.sessionProvider = sessionProvider;
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
        Date date = pricingResults.getDate();

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR, 8);

        date = calendar.getTime();

        String currency = pricingResults.getCurrency();

        log.debug("persist start :" + pricingResults.getDate());

        Iterator<com.opengamma.strata.collect.result.Result<MarkitValuation>> iterator = pricingResults.getResults().iterator();
        while (iterator.hasNext()) {

            com.opengamma.strata.collect.result.Result<MarkitValuation> result = iterator.next();
            log.debug(result.toString());
            for (Value value : result.getValue().getValues()) {
                String tradeId = value.getTradeId();
                Double pv = value.getPv();

                log.debug("tradeId:" + tradeId);

                Iterable<Trade> trades = sessionProvider.get().query(Trade.class, "match (i:Trade {id:\"" + tradeId + "\"}) return i", Collections.emptyMap());
                if(!trades.iterator().hasNext())
                    continue;

                Trade trade = trades.iterator().next();
                trade = sessionProvider.get().load(Trade.class, trade.getId(), 2);
                log.debug(trade.toString());

                Set<Valuation> valuations = trade.getValuations();
                boolean found = false;
                if (valuations != null) {
                    for (Valuation valuation : valuations) {
                        log.debug("date in valuation : " + valuation.getDate());
                        if (valuation.getDate().getYear() == date.getYear() && valuation.getDate().getMonthValue() == date.getMonth() && valuation.getDate().getDayOfMonth() == date.getDay()) {
                            log.debug("existing valuation");
                            //existing date, add or replace the value
                            Set<com.acuo.persist.entity.Value> existedValues = valuation.getValues();
                            for (com.acuo.persist.entity.Value existedValue : existedValues) {
                                if (existedValue.getCurrency().equals(currency) && existedValue.getSource().equalsIgnoreCase("Markit"))
                                    sessionProvider.get().delete(existedValue);
                            }

                            com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                            newValue.setSource("Markit");
                            newValue.setCurrency(com.opengamma.strata.basics.currency.Currency.of(currency));
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

                    valuation.setDate(toLocalDate(calendar.getTime()));
                    log.debug("new valuation:" + valuation.getDate());

                    com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                    newValue.setSource("Markit");
                    newValue.setCurrency(Currency.of(currency));
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
        SwapTrade swapTrade = new SwapTrade();
        Swap swap = new Swap();
        swapTrade.setProduct(swap);
        swapTrade.setType(ProductType.SWAP);

        String query = "match (i:IRS {id:\"" + swapId + "\"}) return i.clearingDate as clearingDate, i.id as id";
        Result result = sessionProvider.get().query(query, Collections.emptyMap());
        if (result.iterator().hasNext()) {
            for (Map<String, Object> entry : result.queryResults())
                swapTrade.setInfo(SwapTradeBuilder.buildTradeInfo(entry));
        }

        query = "match (i:IRS {id:\"" + swapId + "\"})-[r:RECEIVES |PAYS]->(l:Leg) return l.notional as notional, l.resetFrequency as resetFrequency, l.payStart as payStart, l.indexTenor as indexTenor, l.index as index, l.paymentFrequency as paymentFrequency,\n" +
                "l.type as type, l.rollConvention as rollConvention, l.nextCouponPaymentDate as nextCouponPaymentDate, l.payEnd as payEnd, l.refCalendar as refCalendar";
        log.debug("query:" + query);
        result = sessionProvider.get().query(query, Collections.emptyMap());
        if (result.iterator().hasNext()) {
            for (Map<String, Object> entry : result.queryResults()) {
                Swap.SwapLeg leg = SwapTradeBuilder.buildLeg(entry);
                swap.addLeg(leg);
            }
        }

        log.debug("swapTrade:" + swapTrade);


        //load swap object based on swapId
        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
        swapTrades.add(swapTrade);
        return swapTrades;
    }

    public static LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
