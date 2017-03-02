package com.acuo.valuation.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.PricingResults;

import java.util.List;

public interface PricingService {

    PricingResults price(List<Long> swapId);

    PricingResults priceTradesOf(ClientId clientId);

    PricingResults priceTradesUnder(PortfolioId portfolioId);

    PricingResults priceTradesOfType(String type);

}
