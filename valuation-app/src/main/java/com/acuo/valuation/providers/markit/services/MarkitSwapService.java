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
public class MarkitSwapService extends MarkitPricingService implements SwapService {

    @Inject
    public MarkitSwapService(Sender sender, Retriever retriever, Neo4jPersistService sessionProvider) {
        super(sender, retriever, sessionProvider);
    }

    @Override
    public PricingResults getPv(String swapId) {
        try {
            List<SwapTrade> swapTrades = getSwapTrades(swapId);
            return price(swapTrades);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
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
}
