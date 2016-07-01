package com.acuo.valuation.markit.services;

import com.acuo.valuation.markit.product.swap.IrSwapInput;
import com.acuo.valuation.markit.product.swap.IrSwapLegInput;
import com.acuo.valuation.markit.product.swap.IrSwapLegPayDatesInput;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.BusinessDayConvention;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.HolidayCalendarIds;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.common.BuySell;
import com.opengamma.strata.product.swap.*;
import com.opengamma.strata.product.swap.type.FixedIborSwapTemplate;
import com.opengamma.strata.product.swap.type.FixedRateSwapLegConvention;
import com.opengamma.strata.product.swap.type.IborRateSwapLegConvention;
import com.opengamma.strata.product.swap.type.ImmutableFixedIborSwapConvention;
import org.junit.Test;

import java.time.LocalDate;

import static com.opengamma.strata.basics.currency.Currency.USD;
import static com.opengamma.strata.basics.date.BusinessDayConventions.MODIFIED_FOLLOWING;
import static com.opengamma.strata.basics.date.DayCounts.ACT_360;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.GBLO;
import static com.opengamma.strata.basics.date.Tenor.TENOR_10Y;
import static com.opengamma.strata.basics.index.IborIndices.USD_LIBOR_3M;
import static com.opengamma.strata.basics.schedule.Frequency.P6M;

public class StrataTest {

    @Test
    public void testSwapInputToSwapTrade() {
        BusinessDayAdjustment BDA_MOD_FOLLOW = BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO);
        FixedRateSwapLegConvention FIXED = FixedRateSwapLegConvention.of(USD, ACT_360, P6M, BDA_MOD_FOLLOW);
        IborRateSwapLegConvention IBOR = IborRateSwapLegConvention.of(USD_LIBOR_3M);
        ImmutableFixedIborSwapConvention CONV = ImmutableFixedIborSwapConvention.of("USD-Swap", FIXED, IBOR);
        FixedIborSwapTemplate template = FixedIborSwapTemplate.of(TENOR_10Y, CONV);
        SwapTrade trade = template.createTrade(LocalDate.now(), BuySell.SELL, 2_000_000d, 0.5, ReferenceData.standard());
    }

    private static SwapTrade newSwap(IrSwapInput input) {
        TradeInfo info = TradeInfo.of(input.tradeDate);
        SwapTrade trade = SwapTrade.of(info, null);
        return trade;
    }

    private static SwapLeg newSwapLeg(IrSwapLegInput input) {

        /*id = input.id;
        currency = input.currency;
        fixing = input.fixing != null ? new IrSwapLegFixing(input.fixing) : null;
        spread = input.spread;
        rate = input.rate;
        type = input.type;
        daycount = input.daycount;
        notional = input.notional;
        notionalxg = input.notionalxg;
        paydates = new IrSwapLegPayDates(input.payDates);*/

        RatePeriodSwapLeg.builder()
                .type(SwapLegType.valueOf(input.type))
                //.paymentBusinessDayAdjustment(BusinessDayAdjustment.of(BusinessDayConvention.of(MODIFIED_FOLLOWING, HolidayCalendarIds.USNY)))
                .build();

        RatePaymentPeriod.builder()
                .currency(Currency.of(input.currency))
                .dayCount(DayCount.of(input.daycount))
                .notional(input.notional)
                .accrualPeriods()
                .build();

        newAccrual(input.payDates);

        return null;
    }

    private static RateAccrualPeriod newAccrual(IrSwapLegPayDatesInput payDates) {
        return RateAccrualPeriod.builder()
                .startDate(payDates.startDate)
                .endDate(payDates.enddate)
                //.yearFraction(payDates.frequency)
                //.rateComputation(GBPLIBOR3M_2014_09_28)
                .build();
    }
}
