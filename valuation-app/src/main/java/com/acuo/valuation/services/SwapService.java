package com.acuo.valuation.services;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Valuation;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.protocol.results.SwapResults;

import java.util.List;

public interface SwapService {

    PricingResults price(String swapId);

    PricingResults priceClientTrades(String clientId);

    boolean persistMarkitResult(PricingResults pricingResults);

    boolean persistClarusResult(MarginResults marginResults);

    boolean geneareteMarginCall(Agreement agreement, Portfolio portfolio, Valuation valuation);

}
