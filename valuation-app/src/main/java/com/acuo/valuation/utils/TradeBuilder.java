package com.acuo.valuation.utils;

import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.ProductType;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.TradeInfo;
import com.acuo.persist.entity.*;
import com.acuo.persist.entity.enums.PricingProvider;
import com.acuo.persist.ids.TradeId;
import com.opengamma.strata.product.common.PayReceive;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class TradeBuilder {

    private static String TRADE_TYPE_CLEARD = "Cleared";
    private static String TRADE_TYPE_BILATERAL = "Bilateral";

    public Trade build(SwapTrade swapTrade)
    {
        Trade trade = null;
        Swap swap = swapTrade.getProduct();
        TradeInfo tradeInfo = swapTrade.getInfo();

        Set<Leg> payLegs = new HashSet<>();
        Set<Leg> receiveLegs = new HashSet<>();

        if(swapTrade.getType().equals(ProductType.SWAP))
        {
            IRS irs = new IRS();


            irs.setTradeType(tradeInfo.getDerivativeType());

            irs.setPayLegs(payLegs);
            irs.setReceiveLegs(receiveLegs);
            trade = irs;
        }
        else
        if(swapTrade.getType().equals(ProductType.FRA))
        {
            FRA fra = new FRA();
            trade = fra;
            fra.setTradeType(tradeInfo.getDerivativeType());
            fra.setPayLegs(payLegs);
            fra.setReceiveLegs(receiveLegs);
        }

        if(TRADE_TYPE_BILATERAL.equalsIgnoreCase(tradeInfo.getDerivativeType()) && swapTrade.getType().equals(ProductType.SWAP))
        {
            PricingSource pricingSource = new PricingSource();
            pricingSource.setName(PricingProvider.Markit);
            trade.setPricingSource(pricingSource);
        }
        else
        {
            PricingSource pricingSource = new PricingSource();
            pricingSource.setName(PricingProvider.Clarus);
            trade.setPricingSource(pricingSource);
        }


        //trade.setCurrency(tradeInfo.get);
        trade.setTradeId(TradeId.fromString(tradeInfo.getTradeId()));
        trade.setTradeDate(tradeInfo.getTradeDate());
        trade.setMaturity(tradeInfo.getMaturityDate());
        trade.setClearingDate(tradeInfo.getClearedTradeDate());

        //leg
        int legId = 0;
        for(Swap.SwapLeg swapLeg: swap.getLegs())
        {
            Leg leg = new Leg();
            if(PayReceive.PAY.equals(swapLeg.getPayReceive()))
                payLegs.add(leg);
            else
                receiveLegs.add(leg);

            legId++;
            leg.setLegId(tradeInfo.getTradeId()+"-" + legId);
            leg.setType(swapLeg.getType());
            leg.setCurrency(swapLeg.getCurrency());
            leg.setBusinessDayConvention(swapLeg.getPaymentSchedule().getAdjustment().getBusinessDayConvention());
            leg.setPaymentFrequency(swapLeg.getPaymentSchedule().getFrequency());
            leg.setPayStart(swapLeg.getStartDate().getDate());
            leg.setPayEnd(swapLeg.getMaturityDate().getDate());
            leg.setDayCount(swapLeg.getDaycount());
            leg.setNotional(swapLeg.getNotional());
            if(swapLeg.getRate() != null)
            leg.setFixedRate(swapLeg.getRate()/100);
            if(swapLeg.getResetSchedule() != null)
                leg.setResetFrequency(swapLeg.getResetSchedule().getFrequency());
            if(swapLeg.getPaymentSchedule().getAdjustment().getHolidays() != null && swapLeg.getPaymentSchedule().getAdjustment().getHolidays().size() > 0)
            {
                leg.setRefCalendar(swapLeg.getPaymentSchedule().getAdjustment().getHolidays().iterator().next().getName());
            }

            if(swapLeg.getFixing() != null)
            {
                Swap.SwapLegFixing fixing = swapLeg.getFixing();
                leg.setIndex(fixing.getFloatingRateName());
                leg.setIndexTenor(fixing.getTenor());
            }

        }
        return trade;
    }
}
