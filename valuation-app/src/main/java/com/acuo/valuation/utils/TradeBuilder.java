package com.acuo.valuation.utils;

import com.acuo.common.model.product.FRA;
import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.FRATrade;
import com.acuo.common.model.trade.FXSwapTrade;
import com.acuo.common.model.trade.ProductType;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.TradeInfo;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.enums.PricingProvider;
import com.acuo.persist.ids.TradeId;
import com.opengamma.strata.product.common.PayReceive;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class TradeBuilder {

    //private static String TRADE_TYPE_CLEARD = "Cleared";
    private static String TRADE_TYPE_BILATERAL = "Bilateral";

    public static Trade build(com.acuo.common.model.trade.Trade trade) {
        if (trade instanceof SwapTrade) {
            return new SwapBuilder().build((SwapTrade) trade);
        } else if (trade instanceof FRATrade) {
            return new FRABuilder().build((FRATrade) trade);
        } else if (trade instanceof FXSwapTrade) {
            return new FXSwapBuilder().build((FXSwapTrade) trade);
        } else {
            throw new RuntimeException("builder not found for trade " + trade);
        }
    }

    private static class FRABuilder {
        public Trade build(FRATrade fraTrade) {
            FRA fra = fraTrade.getProduct();
            TradeInfo tradeInfo = fraTrade.getInfo();

            Set<Leg> payLegs = new HashSet<>();
            Set<Leg> receiveLegs = new HashSet<>();

            com.acuo.persist.entity.FRA entity = new com.acuo.persist.entity.FRA();
            entity.setTradeType(tradeInfo.getDerivativeType());
            entity.setPayLegs(payLegs);
            entity.setReceiveLegs(receiveLegs);

            if (TRADE_TYPE_BILATERAL.equalsIgnoreCase(tradeInfo.getDerivativeType())) {
                PricingSource pricingSource = new PricingSource();
                pricingSource.setName(PricingProvider.Markit);
                entity.setPricingSource(pricingSource);
            } else {
                PricingSource pricingSource = new PricingSource();
                pricingSource.setName(PricingProvider.Clarus);
                entity.setPricingSource(pricingSource);
            }

            //trade.setCurrency(tradeInfo.get);
            entity.setTradeId(TradeId.fromString(tradeInfo.getTradeId()));
            entity.setTradeDate(tradeInfo.getTradeDate());
            entity.setMaturity(tradeInfo.getMaturityDate());
            entity.setClearingDate(tradeInfo.getClearedTradeDate());

            //leg
            int legId = 0;
            for (FRA.FRALeg fraLeg : fra.getLegs()) {
                Leg leg = new Leg();
                if (PayReceive.PAY.equals(fraLeg.getPayReceive()))
                    payLegs.add(leg);
                else
                    receiveLegs.add(leg);

                legId++;
                leg.setLegId(tradeInfo.getTradeId() + "-" + legId);
                leg.setType(fraLeg.getType());
                leg.setCurrency(fraLeg.getCurrency());
                leg.setPayStart(fraLeg.getStartDate().getDate());
                leg.setPayEnd(fraLeg.getMaturityDate().getDate());
                leg.setDayCount(fraLeg.getDaycount());
                leg.setNotional(fraLeg.getNotional());
                if (fraLeg.getRate() != null)
                    leg.setFixedRate(fraLeg.getRate() / 100);

                if (fraLeg.getFixing() != null) {
                    FRA.FRALegFixing fixing = fraLeg.getFixing();
                    leg.setIndex(fixing.getFloatingRateName());
                    leg.setIndexTenor(fixing.getTenor());
                }
            }
            return entity;
        }
    }

    private static class FXSwapBuilder {
        public Trade build(FXSwapTrade fxSwapTrade) {
            return null;
        }
    }

    private static class SwapBuilder {
        public Trade build(SwapTrade swapTrade) {
            Swap swap = swapTrade.getProduct();
            TradeInfo tradeInfo = swapTrade.getInfo();

            Set<Leg> payLegs = new HashSet<>();
            Set<Leg> receiveLegs = new HashSet<>();

            IRS entity = new IRS();
            entity.setTradeType(tradeInfo.getDerivativeType());
            entity.setPayLegs(payLegs);
            entity.setReceiveLegs(receiveLegs);

            if (TRADE_TYPE_BILATERAL.equalsIgnoreCase(tradeInfo.getDerivativeType()) && swapTrade.getType().equals(ProductType.SWAP)) {
                PricingSource pricingSource = new PricingSource();
                pricingSource.setName(PricingProvider.Markit);
                entity.setPricingSource(pricingSource);
            } else {
                PricingSource pricingSource = new PricingSource();
                pricingSource.setName(PricingProvider.Clarus);
                entity.setPricingSource(pricingSource);
            }

            //trade.setCurrency(tradeInfo.get);
            entity.setTradeId(TradeId.fromString(tradeInfo.getTradeId()));
            entity.setTradeDate(tradeInfo.getTradeDate());
            entity.setMaturity(tradeInfo.getMaturityDate());
            entity.setClearingDate(tradeInfo.getClearedTradeDate());

            //leg
            int legId = 0;
            for (Swap.SwapLeg swapLeg : swap.getLegs()) {
                Leg leg = new Leg();
                if (PayReceive.PAY.equals(swapLeg.getPayReceive()))
                    payLegs.add(leg);
                else
                    receiveLegs.add(leg);

                legId++;
                leg.setLegId(tradeInfo.getTradeId() + "-" + legId);
                leg.setType(swapLeg.getType());
                leg.setCurrency(swapLeg.getCurrency());
                leg.setBusinessDayConvention(swapLeg.getPaymentSchedule().getAdjustment().getBusinessDayConvention());
                leg.setPaymentFrequency(swapLeg.getPaymentSchedule().getFrequency());
                leg.setPayStart(swapLeg.getStartDate().getDate());
                leg.setPayEnd(swapLeg.getMaturityDate().getDate());
                leg.setDayCount(swapLeg.getDaycount());
                leg.setNotional(swapLeg.getNotional());
                if (swapLeg.getRate() != null)
                    leg.setFixedRate(swapLeg.getRate() / 100);
                if (swapLeg.getResetSchedule() != null)
                    leg.setResetFrequency(swapLeg.getResetSchedule().getFrequency());
                if (swapLeg.getPaymentSchedule().getAdjustment().getHolidays() != null && swapLeg.getPaymentSchedule().getAdjustment().getHolidays().size() > 0) {
                    leg.setRefCalendar(swapLeg.getPaymentSchedule().getAdjustment().getHolidays().iterator().next().getName());
                }

                if (swapLeg.getFixing() != null) {
                    Swap.SwapLegFixing fixing = swapLeg.getFixing();
                    leg.setIndex(fixing.getFloatingRateName());
                    leg.setIndexTenor(fixing.getTenor());
                }
            }
            return entity;
        }
    }
}
