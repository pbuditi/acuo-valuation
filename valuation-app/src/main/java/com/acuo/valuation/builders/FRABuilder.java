package com.acuo.valuation.builders;

import com.acuo.common.model.AdjustableDate;
import com.acuo.common.model.AdjustableSchedule;
import com.acuo.common.model.BusinessDayAdjustment;
import com.acuo.common.model.product.FRA;
import com.acuo.common.model.trade.FRATrade;
import com.acuo.common.model.trade.ProductType;
import com.acuo.common.model.trade.TradeInfo;
import com.acuo.persist.entity.Leg;
import com.acuo.persist.entity.PricingSource;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.enums.PricingProvider;
import com.acuo.persist.ids.TradeId;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.date.HolidayCalendarId;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.schedule.RollConvention;
import com.opengamma.strata.product.common.PayReceive;
import com.opengamma.strata.product.swap.FixingRelativeTo;

import java.util.HashSet;
import java.util.Set;

class FRABuilder extends TradeBuilder {
    public Trade build(FRATrade fraTrade) {
        FRA fra = fraTrade.getProduct();
        TradeInfo tradeInfo = fraTrade.getInfo();

        Set<Leg> payLegs = new HashSet<>();
        Set<Leg> receiveLegs = new HashSet<>();

        com.acuo.persist.entity.FRA entity = new com.acuo.persist.entity.FRA();
        entity.setTradeType(tradeInfo.getDerivativeType());
        entity.setPayLegs(payLegs);
        entity.setReceiveLegs(receiveLegs);

        if (TradeBuilder.TRADE_TYPE_BILATERAL.equalsIgnoreCase(tradeInfo.getDerivativeType())) {
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
            if (fraLeg.getMaturityDate().getAdjustment().getHolidays() != null && fraLeg.getMaturityDate().getAdjustment().getHolidays().size() > 0) {
                leg.setRefCalendar(fraLeg.getMaturityDate().getAdjustment().getHolidays().iterator().next().getName());
            }

            if (fraLeg.getFixing() != null) {
                FRA.FRALegFixing fixing = fraLeg.getFixing();
                leg.setIndex(fixing.getFloatingRateName());
                leg.setIndexTenor(fixing.getTenor());
            }
        }
        return entity;
    }

    public FRATrade buildTrade(com.acuo.persist.entity.FRA trade) {
        FRATrade fraTrade = new FRATrade();
        com.acuo.common.model.product.FRA fra = new com.acuo.common.model.product.FRA();
        fraTrade.setProduct(fra);
        fraTrade.setType(ProductType.FRA);

        TradeInfo info = buildTradeInfo(trade);
        info.setTradeId(trade.getTradeId().toString());
        info.setClearedTradeId(trade.getTradeId().toString());
        info.setPortfolio(trade.getPortfolio().getPortfolioId().toString());
        fraTrade.setInfo(info);

        Set<Leg> receiveLegs = trade.getReceiveLegs();
        if(receiveLegs != null)
            for (Leg receiveLeg : receiveLegs) {
                com.acuo.common.model.product.FRA.FRALeg leg = buildFRALeg(1,receiveLeg);
                leg.setPayReceive(PayReceive.RECEIVE);
                leg.setRollConvention(RollConvention.ofDayOfMonth(10));
                fra.addLeg(leg);
            }

        Set<Leg> payLegs = trade.getPayLegs();
        if(payLegs != null)
            for (Leg payLeg : payLegs) {
                com.acuo.common.model.product.FRA.FRALeg leg = buildFRALeg(2, payLeg);
                leg.setPayReceive(PayReceive.PAY);
                leg.setRollConvention(RollConvention.ofDayOfMonth(10));
                leg.setNotional( -1 * leg.getNotional());
                fra.addLeg(leg);
            }
        return fraTrade;
    }

    private com.acuo.common.model.product.FRA.FRALeg buildFRALeg(int id, Leg leg) {
        com.acuo.common.model.product.FRA.FRALeg  result = new com.acuo.common.model.product.FRA.FRALeg ();

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

        if ("FLOAT".equals(leg.getType())) {
            com.acuo.common.model.product.FRA.FRALegFixing swapLegFixing = new com.acuo.common.model.product.FRA.FRALegFixing ();
            result.setFixing(swapLegFixing);

            swapLegFixing.setTenor(leg.getIndexTenor());
            if(swapLegFixing.getTenor() == null)
                swapLegFixing.setTenor(Tenor.TENOR_1D);

            swapLegFixing.setFloatingRateName(leg.getIndex());

            swapLegFixing.setFixingRelativeTo(FixingRelativeTo.PERIOD_START);
        }
        return result;
    }
}
