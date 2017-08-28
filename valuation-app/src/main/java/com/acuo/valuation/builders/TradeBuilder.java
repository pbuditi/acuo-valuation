package com.acuo.valuation.builders;

import com.acuo.common.cache.manager.CacheManager;
import com.acuo.common.cache.manager.Cacheable;
import com.acuo.common.cache.manager.CachedObject;
import com.acuo.common.model.trade.TradeInfo;
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
abstract class TradeBuilder {

    static String TRADE_TYPE_BILATERAL = "Bilateral";
    private static CacheManager cacheManager = new CacheManager();

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
