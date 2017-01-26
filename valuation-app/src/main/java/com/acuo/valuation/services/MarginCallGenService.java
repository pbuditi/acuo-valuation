package com.acuo.valuation.services;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Valuation;

public interface MarginCallGenService {
    boolean geneareteMarginCall(Agreement agreement, Portfolio portfolio, Valuation valuation);
}
