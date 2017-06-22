package com.acuo.valuation.services;

import com.acuo.common.model.trade.Trade;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.MarkitResults;

import java.util.List;

public interface PricingService {

    MarkitResults priceSwapTrades(List<Trade> trades);

    MarkitResults priceTradeIds(List<String> swapId);

    MarkitResults priceTradesOf(ClientId clientId);

    MarkitResults priceTradesUnder(PortfolioId portfolioId);

    MarkitResults priceTradesOfType(String type);

    MarkitResults priceSwapTradesByBulk(List<Trade> trades);

    MarkitResults pricePortfolios(List<PortfolioId> portfolioIds);

}
