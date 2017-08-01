package com.acuo.valuation.builders;

import com.acuo.common.model.trade.FXSwapTrade;
import com.acuo.persist.entity.Trade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class FXSwapBuilder extends TradeBuilder {

    public Trade build(FXSwapTrade trade) {
        log.info("convert {} to Trade entity", trade);
        throw new UnsupportedOperationException("trade " + trade + " not supported");
    }
}
