package com.acuo.valuation.services;

import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.SwapResults;

import java.util.List;

public interface SwapService {

    PricingResults price(String swapId);

    PricingResults priceClientTrades(String clientId);

    boolean persist(PricingResults pricingResults);

}
