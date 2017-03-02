package com.acuo.valuation.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.model.trade.Trade;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.PricingResults;

import java.util.List;

public interface PricingService {

    PricingResults priceSwapTrades(List<SwapTrade> swaps);

    PricingResults priceTradeIds(List<String> swapId);

    PricingResults priceTradesOf(ClientId clientId);

    PricingResults priceTradesUnder(PortfolioId portfolioId);

    PricingResults priceTradesOfType(String type);

}
