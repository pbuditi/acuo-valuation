package com.acuo.valuation.utils;

import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.TradeInfo;
import org.neo4j.ogm.model.Result;

import java.util.Map;

public class SwapTradeBuilder {

    public static SwapTrade buildIRS(Map<String, Object> entry)
    {
        SwapTrade swapTrade = new SwapTrade();

        TradeInfo tradeInfo = new TradeInfo();
        swapTrade.setInfo(tradeInfo);
        tradeInfo.setTradeId((String)entry.get("id"));

        Swap swap = new Swap();

        return swapTrade;
    }

    public static Swap.SwapLeg buildLeg(Map<String, Object> entry)
    {
        Swap.SwapLeg leg = new Swap.SwapLeg();

        if(entry.get("notional") != null)
        {
            leg.setNotional((Double)entry.get("notional"));
        }

        if(entry.get("fixedRate") != null)
        {
            leg.setRate((Double)entry.get("fixedRate"));
        }


        return leg;
    }
}
