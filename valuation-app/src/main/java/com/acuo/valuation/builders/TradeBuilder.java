package com.acuo.valuation.builders;

import com.acuo.common.cache.manager.CacheManager;
import com.acuo.common.cache.manager.Cacheable;
import com.acuo.common.cache.manager.CachedObject;
import com.acuo.common.model.trade.FRATrade;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.TradeInfo;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Leg;
import com.acuo.persist.entity.Trade;
import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.date.HolidayCalendarId;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.basics.date.ImmutableHolidayCalendar;
import lombok.extern.slf4j.Slf4j;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

@Slf4j
public class TradeBuilder {

    //private static String TRADE_TYPE_CLEARD = "Cleared";
    static String TRADE_TYPE_BILATERAL = "Bilateral";
    private static CacheManager cacheManager = new CacheManager();

    public static Trade build(com.acuo.common.model.trade.Trade trade) {
        if (trade instanceof SwapTrade) {
            return new SwapBuilder().build((SwapTrade) trade);
        }

        if (trade instanceof FRATrade) {
            return new FRABuilder().build((FRATrade) trade);
        }

        throw new UnsupportedOperationException("trade " + trade + " not supported");
    }

    public static com.acuo.common.model.trade.Trade buildTrade(Trade trade) {

        if (trade instanceof com.acuo.persist.entity.FRA) {
            return new FRABuilder().buildTrade((com.acuo.persist.entity.FRA)trade);
        }

        if (trade instanceof IRS) {
            return new SwapBuilder().buildTrade((com.acuo.persist.entity.IRS)trade);
        }

        throw new UnsupportedOperationException("trade " + trade + " not supported");
    }

    TradeInfo buildTradeInfo(Trade trade) {
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.setTradeId(trade.getTradeId().toString());
        tradeInfo.setClearedTradeDate(trade.getClearingDate());
        tradeInfo.setTradeDate(trade.getTradeDate());
        tradeInfo.setBook(trade.getAccount().getAccountId());
        return tradeInfo;
    }

    HolidayCalendarId holidays(Leg leg) {
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
