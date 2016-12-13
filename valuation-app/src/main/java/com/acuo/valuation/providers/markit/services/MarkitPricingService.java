package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.Valuation;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.Value;
import com.acuo.valuation.services.PricingService;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class MarkitPricingService implements PricingService {

    private final Sender sender;
    private final Retriever retriever;

    private final Neo4jPersistService sessionProvider;

    @Inject
    public MarkitPricingService(Sender sender, Retriever retriever, Neo4jPersistService sessionProvider) {
        this.sender = sender;
        this.retriever = retriever;
        this.sessionProvider = sessionProvider;
    }

    @Override
    public PricingResults price(List<SwapTrade> swaps) {

        Report report = sender.send(swaps);

        Predicate<? super String> errorReport = (Predicate<String>) tradeId -> {
            List<Report.Item> items = report.itemsPerTradeId().get(tradeId);
            if (items.stream().anyMatch(item -> "ERROR".equals(item.getType()))) {
                return false;
            }
            return true;
        };

        List<String> tradeIds = swaps
                .stream()
                .map(swap -> swap.getInfo().getTradeId())
                .filter(errorReport)
                .collect(Collectors.toList());

        return retriever.retrieve(report.valuationDate(), tradeIds);
    }

    @Override
    public boolean savePv(PricingResults pricingResults)
    {


//        Collection<Trade> trades = sessionProvider.get().loadAll(Trade.class, 3);
//
//        for(Trade trade : trades)
//            log.debug(trade.toString());

        Date date = pricingResults.getDate();

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR, 8);

        date = calendar.getTime();

        String currency = pricingResults.getCurrency();

        log.debug("savePv start :" + pricingResults.getDate());

        Iterator<Result<MarkitValuation>>  iterator = pricingResults.getResults().iterator();
        while(iterator.hasNext())
        {

            Result<MarkitValuation> result = iterator.next();
            log.debug(result.toString());
            for(Value value : result.getValue().getValues())
            {
                String tradeId = value.getTradeId();
                Double pv = value.getPv();

                log.debug("tradeId:" + tradeId);

                Iterable<Trade> trades = sessionProvider.get().query(Trade.class, "match (i:Trade {id:\"" + tradeId + "\"}) return i", Collections.emptyMap());

                Trade trade = trades.iterator().next();

                trade = sessionProvider.get().load(Trade.class, trade.getId(), 2);

                log.debug(trade.toString());

                Set<Valuation> valuations = trade.getValuations();

                boolean found = false;
                for(Valuation valuation : valuations)
                {
                    log.debug("date in valuation : " + valuation.getDate());
                    if(valuation.getDate().getYear() == date.getYear() && valuation.getDate().getMonth() == date.getMonth() && valuation.getDate().getDay() == date.getDay())
                    {
                        log.debug("existing valuation");
                        //existing date, add or replace the value
                        Set<com.acuo.persist.entity.Value>  existedValues = valuation.getValues();
                        for(com.acuo.persist.entity.Value existedValue : existedValues)
                        {
                            if(existedValue.getCurrency().equals(currency) && existedValue.getSource().equalsIgnoreCase("Markit"))
                                sessionProvider.get().delete(existedValue);
                        }

                        com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                        newValue.setSource("Markit");
                        newValue.setCurrency(currency);
                        newValue.setPv(value.getPv());

                        existedValues = valuation.getValues();
                        existedValues.add(newValue);

                        valuation.setValues(existedValues);

                        sessionProvider.get().save(valuation, 2);

                        found = true;
                        break;


                    }

                }

                if(!found)
                {
                    //new valutaion

                    Valuation valuation = new Valuation();

                    valuation.setDate(calendar.getTime());
                    log.debug("new valuation:" + valuation.getDate());

                    com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();

                    newValue.setSource("Markit");
                    newValue.setCurrency(currency);
                    newValue.setPv(value.getPv());

                    Set<com.acuo.persist.entity.Value> values = new HashSet<com.acuo.persist.entity.Value>();

                    values.add(newValue);

                    valuation.setValues(values);

                    trade.getValuations().add(valuation);


                    sessionProvider.get().save(trade, 2);


                }


            }

        }


        return true;
    }

}
