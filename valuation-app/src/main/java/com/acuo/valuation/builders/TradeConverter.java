package com.acuo.valuation.builders;

import com.acuo.common.model.trade.FRATrade;
import com.acuo.common.model.trade.FXSwapTrade;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradeConverter {

    private static SwapBuilder swapBuilder = new SwapBuilder();
    private static FRABuilder fraBuilder = new FRABuilder();
    private static FXSwapBuilder fxSwapBuilder = new FXSwapBuilder();

    public static Trade build(com.acuo.common.model.trade.Trade trade) {

        if (trade instanceof FRATrade) {
            return fraBuilder.build((FRATrade) trade);
        }

        if (trade instanceof SwapTrade) {
            return swapBuilder.build((SwapTrade) trade);
        }

        if(trade instanceof FXSwapTrade) {
            return fxSwapBuilder.build((FXSwapTrade) trade);
        }

        throw new UnsupportedOperationException("trade " + trade + " not supported");
    }

    public static com.acuo.common.model.trade.Trade buildTrade(Trade trade) {

        if (trade instanceof com.acuo.persist.entity.FRA) {
            return fraBuilder.buildTrade((com.acuo.persist.entity.FRA)trade);
        }

        if (trade instanceof IRS) {
            return swapBuilder.buildTrade((com.acuo.persist.entity.IRS)trade);
        }

        throw new UnsupportedOperationException("trade " + trade + " not supported");
    }
}
