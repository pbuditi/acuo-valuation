package com.acuo.valuation.utils;

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
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendarId;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.basics.index.FloatingRateName;
import com.opengamma.strata.basics.schedule.Frequency;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SwapTradeBuilder {

    public static SwapTrade buildTrade(IRS trade) {
        SwapTrade swapTrade = new SwapTrade();
        Swap swap = new Swap();
        swapTrade.setProduct(swap);
        swapTrade.setType(ProductType.SWAP);

        TradeInfo info = SwapTradeBuilder.buildTradeInfo(trade);
        info.setTradeId(trade.getTradeId().toString());
        swapTrade.setInfo(info);

        Set<Leg> payLegs = trade.getPayLegs();
        for (Leg payLeg : payLegs) {
            Swap.SwapLeg leg = SwapTradeBuilder.buildLeg(1, payLeg);
            swap.addLeg(leg);
        }

        Set<Leg> receiveLegs = trade.getReceiveLegs();
        for (Leg receiveLeg : receiveLegs) {
            Swap.SwapLeg leg = SwapTradeBuilder.buildLeg(2,receiveLeg);
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

        //if (entry.get("payStart") != null) {
            AdjustableDate adjustableDate = new AdjustableDate();
            adjustableDate.setDate(leg.getPayStart());
        BusinessDayAdjustment adjustment = new BusinessDayAdjustment();
        adjustment.setBusinessDayConvention(leg.getBusinessDayConvention());
        HolidayCalendarId holidays = HolidayCalendars.NO_HOLIDAYS.getId();
        try  {
            holidays = HolidayCalendars.of(leg.getRefCalendar()).getId();
        } catch(Exception e) {log.error(e.getMessage(),e);}
        adjustment.setHolidays(ImmutableSet.of(holidays));
        adjustableDate.setAdjustment(adjustment);
            result.setStartDate(adjustableDate);
        //}

        //if (entry.get("payEnd") != null) {
            adjustableDate = new AdjustableDate();
            adjustableDate.setDate(leg.getPayEnd());
        adjustableDate.setAdjustment(adjustment);
            result.setMaturityDate(adjustableDate);
        //}

        //failuire at this time
        //if (entry.get("paymentFrequency") != null) {
             AdjustableSchedule adjustableSchedule = new AdjustableSchedule();
             log.debug("paymentFrequency:" + leg.getPaymentFrequency());
             adjustableSchedule.setFrequency(leg.getPaymentFrequency());
        adjustableSchedule.setAdjustment(adjustment);
             result.setPaymentSchedule(adjustableSchedule);
        //}


        Swap.SwapLegFixing swapLegFixing = new Swap.SwapLegFixing();
        result.setFixing(swapLegFixing);

        //if (entry.get("indexTenor") != null)
            swapLegFixing.setTenor(leg.getIndexTenor());

        //if (entry.get("index") != null)
            swapLegFixing.setFloatingRateName(leg.getIndex());


        return result;
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

        //failuire at this time
        try {
            if (entry.get("paymentFrequency") != null) {
                AdjustableSchedule adjustableSchedule = new AdjustableSchedule();
                log.debug("paymentFrequency:" + (String) entry.get("paymentFrequency"));
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
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yy");
            Date date = sdf.parse(s);
            Instant instant = date.toInstant();
            ZoneId zone = ZoneId.systemDefault();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
            return localDateTime.toLocalDate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static Frequency parseFrequency(String s) {
        if (s == null)
            return null;

        if (s.contains("M"))
            return Frequency.ofMonths(Integer.parseInt(s.substring(0, s.length() - 1)));

        if (s.contains("T"))
            return Frequency.ofYears(Integer.parseInt(s.substring(0, s.length() - 1)));

        if (s.contains("W"))
            return Frequency.ofWeeks(Integer.parseInt(s.substring(0, s.length() - 1)));

        if (s.contains("D"))
            return Frequency.ofDays(Integer.parseInt(s.substring(0, s.length() - 1)));
        return null;
    }
}
