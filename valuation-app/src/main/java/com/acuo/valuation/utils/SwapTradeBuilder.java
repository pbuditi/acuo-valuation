package com.acuo.valuation.utils;

import com.acuo.common.model.AdjustableSchedule;
import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.TradeInfo;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.index.FloatingRateName;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.RollConvention;
import com.acuo.common.model.AdjustableDate;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.E;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.ogm.model.Result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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

    public static TradeInfo buildTradeInfo(Map<String, Object> entry)
    {
        TradeInfo tradeInfo = new TradeInfo();

        if(entry.get("id") != null)
            tradeInfo.setTradeId((String)entry.get("id"));

        if(entry.get("clearingDate") != null)
            tradeInfo.setClearedTradeDate(StringToLocalDate((String)entry.get("clearingDate")));
        return tradeInfo;
    }

    public static Swap.SwapLeg buildLeg(Map<String, Object> entry)
    {
        Swap.SwapLeg leg = new Swap.SwapLeg();

        if(entry.get("notional") != null)
            leg.setNotional((Double)entry.get("notional"));

        if(entry.get("fixedRate") != null)
            leg.setRate((Double)entry.get("fixedRate"));

        if(entry.get("type") != null)
            leg.setType((String)entry.get("type"));

        //failure at this time
        try {
            if(entry.get("rollConvention") != null)
                leg.setRollConvention(RollConvention.of((String)entry.get("rollConvention")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



        if(entry.get("payStart") != null)
        {
            AdjustableDate adjustableDate = new AdjustableDate();
            adjustableDate.setDate(StringToLocalDate((String)entry.get("payStart")));
            leg.setStartDate(adjustableDate);
        }

        if(entry.get("payEnd") != null)
        {
            AdjustableDate adjustableDate = new AdjustableDate();
            adjustableDate.setDate(StringToLocalDate((String)entry.get("payEnd")));
            leg.setMaturityDate(adjustableDate);
        }

        //failuire at this time
        try {
            if(entry.get("paymentFrequency") != null)
            {
                AdjustableSchedule adjustableSchedule = new AdjustableSchedule();
                adjustableSchedule.setFrequency(Frequency.parse((String)entry.get("paymentFrequency")));
                leg.setPaymentSchedule(adjustableSchedule);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        Swap.SwapLegFixing swapLegFixing = new Swap.SwapLegFixing();
        leg.setFixing(swapLegFixing);

        if(entry.get("indexTenor") != null)
            swapLegFixing.setTenor(Tenor.parse((String)entry.get("indexTenor")));

        if(entry.get("index") != null)
            swapLegFixing.setFloatingRateName(FloatingRateName.of((String)entry.get("index")));




        return leg;
    }

    public static LocalDate StringToLocalDate(String s) {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yy");
            Date date = sdf.parse(s);
            Instant instant = date.toInstant();
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            return localDateTime.toLocalDate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
