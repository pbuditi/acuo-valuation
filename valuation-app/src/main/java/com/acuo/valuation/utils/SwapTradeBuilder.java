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
import com.opengamma.strata.basics.date.*;
import com.opengamma.strata.basics.index.FloatingRateName;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.RollConvention;
import com.opengamma.strata.product.common.PayReceive;
import com.opengamma.strata.product.swap.FixingRelativeTo;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

@Slf4j
public class SwapTradeBuilder {

    static CacheManager cacheManager = CacheManager.getInstance();

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

    public static TradeInfo buildTradeInfo(Trade trade) {
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.setTradeId(trade.getTradeId().toString());
        tradeInfo.setClearedTradeDate(trade.getClearingDate());
        tradeInfo.setTradeDate(trade.getTradeDate());
        tradeInfo.setBook(trade.getAccount().getAccountId());
        return tradeInfo;
    }

    public static Swap.SwapLeg buildLeg(int id, Leg leg) {
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
            HolidayCalendarId holidays = HolidayCalendars.NO_HOLIDAYS.getId();
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

    public static Swap.SwapLeg buildLeg(Map<String, Object> entry) {
        Swap.SwapLeg leg = new Swap.SwapLeg();

        if (entry.get("notional") != null)
            leg.setNotional((Double) entry.get("notional"));

        if (entry.get("fixedRate") != null)
            leg.setRate((Double) entry.get("fixedRate"));

        if (entry.get("type") != null)
            leg.setType((String) entry.get("type"));


        if (entry.get("payStart") != null) {
            AdjustableDate adjustableDate = new AdjustableDate();
            adjustableDate.setDate(StringToLocalDate((String) entry.get("payStart")));
            leg.setStartDate(adjustableDate);
        }

        if (entry.get("payEnd") != null) {
            AdjustableDate adjustableDate = new AdjustableDate();
            adjustableDate.setDate(StringToLocalDate((String) entry.get("payEnd")));
            leg.setMaturityDate(adjustableDate);
        }

        try {
            if (entry.get("paymentFrequency") != null) {
                AdjustableSchedule adjustableSchedule = new AdjustableSchedule();
                adjustableSchedule.setFrequency(parseFrequency((String) entry.get("paymentFrequency")));
                leg.setPaymentSchedule(adjustableSchedule);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        Swap.SwapLegFixing swapLegFixing = new Swap.SwapLegFixing();
        leg.setFixing(swapLegFixing);

        if (entry.get("indexTenor") != null)
            swapLegFixing.setTenor(Tenor.parse((String) entry.get("indexTenor")));

        if (entry.get("index") != null)
            swapLegFixing.setFloatingRateName(FloatingRateName.of((String) entry.get("index")));

        return leg;
    }

    public static LocalDate StringToLocalDate(String s) {
        Cacheable value = cacheManager.getCache(s);
        if (value != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yy");
                Date date = sdf.parse(s);
                Instant instant = date.toInstant();
                ZoneId zone = ZoneId.systemDefault();
                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
                value = new CachedObject(localDateTime.toLocalDate(), s, 0);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                value = new CachedObject(null, s, 0);
            }
        }
        return (LocalDate)value.getObject();
    }

    public static Frequency parseFrequency(String s) {

        if (s == null)
            return null;

        Cacheable value = cacheManager.getCache(s);
        if (value != null) {
            Frequency frequency = null;
            if (s.contains("M")) {
                frequency = Frequency.ofMonths(Integer.parseInt(s.substring(0, s.length() - 1)));
            }

            if (s.contains("T")) {
                frequency = Frequency.ofYears(Integer.parseInt(s.substring(0, s.length() - 1)));
            }

            if (s.contains("W")) {
                frequency = Frequency.ofWeeks(Integer.parseInt(s.substring(0, s.length() - 1)));
            }

            if (s.contains("D")) {
                frequency = Frequency.ofDays(Integer.parseInt(s.substring(0, s.length() - 1)));
            }

            value = new CachedObject(frequency, s, 0);
        }
        return (Frequency)value.getObject();
    }
}
