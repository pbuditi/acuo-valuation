package com.acuo.valuation.builders;

import com.acuo.common.model.AdjustableDate;
import com.acuo.common.model.AdjustableSchedule;
import com.acuo.common.model.BusinessDayAdjustment;
import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.ProductType;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.TradeInfo;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.enums.PricingProvider;
import com.acuo.common.model.ids.TradeId;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.date.HolidayCalendarId;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.schedule.RollConvention;
import com.opengamma.strata.product.common.PayReceive;
import com.opengamma.strata.product.swap.FixingRelativeTo;

import java.util.HashSet;
import java.util.Set;

class SwapBuilder extends TradeBuilder {

    public Trade build(SwapTrade swapTrade) {
        Swap swap = swapTrade.getProduct();
        TradeInfo tradeInfo = swapTrade.getInfo();

        Set<Leg> payLegs = new HashSet<>();
        Set<Leg> receiveLegs = new HashSet<>();

        IRS entity = new IRS();
        entity.setTradeType(tradeInfo.getDerivativeType());
        entity.setPayLegs(payLegs);
        entity.setReceiveLegs(receiveLegs);

        if (TradeBuilder.TRADE_TYPE_BILATERAL.equalsIgnoreCase(tradeInfo.getDerivativeType()) && swapTrade.getType().equals(ProductType.SWAP)) {
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
        entity.setTradeTime(tradeInfo.getTradeTime() != null ? tradeInfo.getTradeTime().toLocalTime() : null);
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

    public SwapTrade buildTrade(IRS trade) {
        SwapTrade swapTrade = new SwapTrade();
        Swap swap = new Swap();
        swapTrade.setProduct(swap);
        swapTrade.setType(ProductType.SWAP);

        TradeInfo info = buildTradeInfo(trade);
        info.setTradeId(trade.getTradeId().toString());
        info.setClearedTradeId(trade.getTradeId().toString());
        info.setPortfolio(trade.getPortfolio().getPortfolioId().toString());
        swapTrade.setInfo(info);

        Set<Leg> receiveLegs = trade.getReceiveLegs();
        if(receiveLegs != null)
            for (Leg receiveLeg : receiveLegs) {
                Swap.SwapLeg leg = buildLeg(1,receiveLeg);
                leg.setPayReceive(PayReceive.RECEIVE);
                leg.setRollConvention(RollConvention.ofDayOfMonth(10));
                swap.addLeg(leg);
            }

        Set<Leg> payLegs = trade.getPayLegs();
        if(payLegs != null)
            for (Leg payLeg : payLegs) {
                Swap.SwapLeg leg = buildLeg(2, payLeg);
                leg.setPayReceive(PayReceive.PAY);
                leg.setRollConvention(RollConvention.ofDayOfMonth(10));
                leg.setNotional( -1 * leg.getNotional());
                swap.addLeg(leg);
            }

        return swapTrade;
    }

    private Swap.SwapLeg buildLeg(int id, Leg leg) {
        Swap.SwapLeg result = new Swap.SwapLeg();

        result.setId(id);
        result.setCurrency(leg.getCurrency());
        result.setNotional(leg.getNotional());
        result.setRate(leg.getFixedRate());
        result.setDaycount(leg.getDayCount());
        result.setType(leg.getType());

        AdjustableDate adjustableDate = new AdjustableDate();
        adjustableDate.setDate(leg.getPayStart());
        BusinessDayAdjustment adjustment = new BusinessDayAdjustment();
        adjustment.setBusinessDayConvention(leg.getBusinessDayConvention());

        HolidayCalendarId holidays = holidays(leg);

        adjustment.setHolidays(ImmutableSet.of(holidays));
        adjustableDate.setAdjustment(adjustment);
        result.setStartDate(adjustableDate);

        adjustableDate = new AdjustableDate();
        adjustableDate.setDate(leg.getPayEnd());
        adjustableDate.setAdjustment(adjustment);
        result.setMaturityDate(adjustableDate);

        AdjustableSchedule adjustableSchedule = new AdjustableSchedule();
        adjustableSchedule.setFrequency(leg.getPaymentFrequency());
        adjustableSchedule.setAdjustment(adjustment);
        result.setPaymentSchedule(adjustableSchedule);
        result.setCalculationSchedule(adjustableSchedule);

        if ("FLOAT".equals(leg.getType())) {
            Swap.SwapLegFixing fixing = new Swap.SwapLegFixing();
            result.setFixing(fixing);

            fixing.setTenor(leg.getIndexTenor());
            if(fixing.getTenor() == null)
                fixing.setTenor(Tenor.TENOR_1D);

            fixing.setFloatingRateName(leg.getIndex());

            fixing.setFixingRelativeTo(FixingRelativeTo.PERIOD_START);
        }
        return result;
    }
}
