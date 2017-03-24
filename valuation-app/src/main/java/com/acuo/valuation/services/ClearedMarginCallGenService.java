package com.acuo.valuation.services;

import com.acuo.persist.entity.*;

public interface ClearedMarginCallGenService {

    MarginCall geneareteMarginCall(Agreement agreement, Portfolio portfolio, Valuation<MarginValuation> valuation);
}
