package com.acuo.valuation.services;

import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.SwapResults;

public interface SwapService {

    PricingResults price(String swapId);

    boolean persist(PricingResults pricingResults);

}
