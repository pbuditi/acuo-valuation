package com.acuo.valuation.utils;

import com.acuo.common.cache.manager.CacheManager;
import com.acuo.common.cache.manager.Cacheable;
import com.acuo.common.cache.manager.CachedObject;
import com.acuo.common.model.AdjustableDate;
import com.acuo.common.model.AdjustableSchedule;
import com.acuo.common.model.BusinessDayAdjustment;
import com.acuo.common.model.product.Swap;
import com.acuo.common.model.trade.ProductType;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.TradeInfo;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.persist.entity.Trade;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.date.HolidayCalendarId;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.date.ImmutableHolidayCalendar;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.schedule.RollConvention;
import com.opengamma.strata.product.common.PayReceive;
import com.opengamma.strata.product.swap.FixingRelativeTo;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

@Slf4j
public class SwapTradeBuilder {

    private static CacheManager cacheManager = new CacheManager();

    public static SwapTrade buildTrade(IRS trade) {
        SwapTrade swapTrade = new SwapTrade();
        Swap swap = new Swap();
        swapTrade.setProduct(swap);
        swapTrade.setType(ProductType.SWAP);

        TradeInfo info = SwapTradeBuilder.buildTradeInfo(trade);
        info.setTradeId(trade.getTradeId().toString());
        info.setClearedTradeId(trade.getTradeId().toString());
        info.setPortfolio(trade.getPortfolio().getPortfolioId().toString());
        swapTrade.setInfo(info);

        Set<Leg> receiveLegs = trade.getReceiveLegs();
        if(receiveLegs != null)
        for (Leg receiveLeg : receiveLegs) {
            Swap.SwapLeg leg = SwapTradeBuilder.buildLeg(1,receiveLeg);
            leg.setPayReceive(PayReceive.RECEIVE);
            leg.setRollConvention(RollConvention.ofDayOfMonth(10));
            swap.addLeg(leg);
        }

        Set<Leg> payLegs = trade.getPayLegs();
        if(payLegs != null)
            for (Leg payLeg : payLegs) {
                Swap.SwapLeg leg = SwapTradeBuilder.buildLeg(2, payLeg);
                leg.setPayReceive(PayReceive.PAY);
                leg.setRollConvention(RollConvention.ofDayOfMonth(10));
                leg.setNotional( -1 * leg.getNotional());
                swap.addLeg(leg);
            }

        return swapTrade;
    }

    private static TradeInfo buildTradeInfo(Trade trade) {
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.setTradeId(trade.getTradeId().toString());
        tradeInfo.setClearedTradeDate(trade.getClearingDate());
        tradeInfo.setTradeDate(trade.getTradeDate());
        tradeInfo.setBook(trade.getAccount().getAccountId());
        return tradeInfo;
    }

    private static Swap.SwapLeg buildLeg(int id, Leg leg) {
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
            Swap.SwapLegFixing swapLegFixing = new Swap.SwapLegFixing();
            result.setFixing(swapLegFixing);

            swapLegFixing.setTenor(leg.getIndexTenor());
            if(swapLegFixing.getTenor() == null)
                swapLegFixing.setTenor(Tenor.TENOR_1D);

            swapLegFixing.setFloatingRateName(leg.getIndex());

            swapLegFixing.setFixingRelativeTo(FixingRelativeTo.PERIOD_START);
        }
        return result;
    }

    private static HolidayCalendarId holidays(Leg leg) {
        String refCalendar = leg.getRefCalendar();
        Cacheable value = cacheManager.getCache(refCalendar);
        if (value == null) {
            HolidayCalendarId holidays;
            try {
                holidays = HolidayCalendars.of(refCalendar).getId();
            } catch (Exception e) {
                log.warn(e.getMessage());
                holidays = ImmutableHolidayCalendar.of(HolidayCalendarId.of(refCalendar), ImmutableList.of(), SATURDAY, SUNDAY).getId();
            }
            value = new CachedObject(holidays, refCalendar, 0);
            cacheManager.putCache(value);
        }
        return (HolidayCalendarId)value.getObject();
    }
}
