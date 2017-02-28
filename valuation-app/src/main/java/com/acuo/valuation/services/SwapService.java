package com.acuo.valuation.services;

import com.acuo.valuation.jackson.MarginCallDetail;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.PricingResults;

import java.util.List;

public interface SwapService {

    MarginCallDetail price(List<String> swapId);

    MarginCallDetail price(List<String> swapId, boolean isGenCounterpart);

    PricingResults priceClientTrades(String clientId);

    MarginCallDetail persistMarkitResult(PricingResults pricingResults, boolean isGenCounterpart);

    boolean persistClarusResult(MarginResults marginResults);

    MarginCallDetail pricePortfolio(String id);

    MarginCallDetail valuationAllBilateralIRS();

}
