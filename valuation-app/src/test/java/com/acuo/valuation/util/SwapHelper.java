package com.acuo.valuation.util;

import com.acuo.common.marshal.LocalDateAdapter;
import com.acuo.valuation.markit.requests.swap.IrSwapInput;
import com.acuo.valuation.markit.requests.swap.IrSwapLegFixingInput;
import com.acuo.valuation.markit.requests.swap.IrSwapLegInput;
import com.acuo.valuation.markit.requests.swap.IrSwapLegPayDatesInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwapHelper {

    public static IrSwapInput irSwapInput() throws Exception {
        IrSwapInput input = new IrSwapInput();
        input.tradeId = "TRADE_ABC";
        input.tradeDate = new LocalDateAdapter().unmarshal("2016-06-16");
        input.book = "BOOK";
        input.legs = new ArrayList<>();
        input.legs.addAll(irSwapLegs());
        return input;
    }

    private static List<IrSwapLegInput> irSwapLegs() throws Exception {

        IrSwapLegInput fixed = new IrSwapLegInput();
        fixed.id = 1;
        fixed.currency = "USD";
        fixed.fixing = null;
        fixed.spread = null;
        fixed.rate = 0.0533;
        fixed.type = "FIXED";
        fixed.daycount = "30360";
        fixed.notional = 1000000d;
        fixed.notionalxg = "NEITHER";
        fixed.payDates = payDates();

        IrSwapLegInput floating = new IrSwapLegInput();
        floating.id = 2;
        floating.currency = "USD";
        floating.fixing = fixing();
        floating.spread = 0d;
        floating.rate = null;
        floating.type = "FLOAT";
        floating.daycount = "A360";
        floating.notional = -1000000d;
        floating.notionalxg = "NEITHER";
        floating.payDates = payDates();

        return Arrays.asList(fixed, floating);
    }

    private static IrSwapLegFixingInput fixing() {
        IrSwapLegFixingInput fixing = new IrSwapLegFixingInput();
        fixing.name = "USD-LIBOR-BBA";
        fixing.term = "3M";
        fixing.arrears = false;
        return null;
    }

    private static IrSwapLegPayDatesInput payDates() throws Exception {
        IrSwapLegPayDatesInput payDatesInput = new IrSwapLegPayDatesInput();
        payDatesInput.startDate = new LocalDateAdapter().unmarshal("2015-01-20");
        payDatesInput.frequency = "3M";
        payDatesInput.enddate = new LocalDateAdapter().unmarshal("2020-01-20");
        payDatesInput.rollCode = "MODFOLL";
        payDatesInput.adjust = true;
        payDatesInput.eom = false;
        return payDatesInput;
    }
}
