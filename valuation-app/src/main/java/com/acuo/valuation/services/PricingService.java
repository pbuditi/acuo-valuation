package com.acuo.valuation.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.results.PricingResults;

import java.util.List;

public interface PricingService {

    PricingResults price(List<SwapTrade> swap);

    boolean savePv(PricingResults pricingResults);

}
