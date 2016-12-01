package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.ProductType;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.core.Neo4jPersistService;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.SwapService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.model.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
@Slf4j
public class MarkitSwapService implements SwapService {

    private final Sender sender;
    private final Retriever retriever;
    private final Neo4jPersistService sessionProvider;

    @Inject
    public MarkitSwapService(Sender sender, Retriever retriever, Neo4jPersistService sessionProvider) {
        this.sender = sender;
        this.retriever = retriever;
        this.sessionProvider = sessionProvider;
    }


    @Override
    public PricingResults getPv(String swapId)
    {
        try
        {
            Iterable<IRS> irss = sessionProvider.get().loadAll(IRS.class,1);
            for(IRS irs: irss)
                log.debug(irs.toString());
            SwapTrade swapTrade = new SwapTrade();
            Swap swap = new Swap();
            swapTrade.setProduct(swap);
            swapTrade.setType(ProductType.SWAP);

            String query = "match (i:IRS {id:\"" + swapId + "\"}) return i.clearingDate as clearingDate, i.id as id";
            Result result = sessionProvider.get().query(query, Collections.emptyMap());
            if(result.iterator().hasNext())
            {
                for(Map<String, Object> entry : result.queryResults())
                    swapTrade.setInfo(SwapTradeBuilder.buildTradeInfo(entry));
            }


//        String query = "MATCH (n:IRS {id:\"" + swapId + "\"}) RETURN n.payFreqPay AS payFreqPay, n.notional AS notional, n.payFreqReceive as payFreqReceive, n.maturity as maturity, n.markToMarketT as markToMarketT, n.clearingDate as clearingDate,\n" +
//                "n.indexTenor as indexTenor, n.index as index, n.markToMarketTm1 as markToMarketTm1, n.legPay as legPay, n.fixedRate as fixedRate, n.currency as currency, n.id as id, n.resetFreq as resetFreq, n.nextCouponPaymentDate as nextCouponPaymentDate";
            query = "match (i:IRS {id:\"" + swapId + "\"})-[r:RECEIVES |PAYS]->(l:Leg) return l.notional as notional, l.resetFrequency as resetFrequency, l.payStart as payStart, l.indexTenor as indexTenor, l.index as index, l.paymentFrequency as paymentFrequency,\n" +
                    "l.type as type, l.rollConvention as rollConvention, l.nextCouponPaymentDate as nextCouponPaymentDate, l.payEnd as payEnd, l.refCalendar as refCalendar";
            log.debug("query:" + query);
            result = sessionProvider.get().query(query, Collections.emptyMap());
            if(result.iterator().hasNext())
            {
                for(Map<String, Object> entry : result.queryResults())
                {
                    Swap.SwapLeg leg = SwapTradeBuilder.buildLeg(entry);
                    swap.addLeg(leg);
                }
            }

            log.debug("swapTrade:" + swapTrade);



            //load swap object based on swapId

            List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
            swapTrades.add(swapTrade);

            Report report = sender.send(swapTrades);

            Predicate<? super String> errorReport = (Predicate<String>) tradeId -> {
                List<Report.Item> items = report.itemsPerTradeId().get(tradeId);
                if (items.stream().anyMatch(item -> "ERROR".equals(item.getType()))) {
                    return false;
                }
                return true;
            };

            List<String> tradeIds = swapTrades
                    .stream()
                    .map(swapItem -> swapItem.getInfo().getTradeId())
                    .filter(errorReport)
                    .collect(Collectors.toList());

            //get the response,parse and return
            return retriever.retrieve(report.valuationDate(), tradeIds);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;

    }
}
