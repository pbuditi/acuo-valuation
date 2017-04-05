package com.acuo.valuation.services;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.Valuation;

import java.time.LocalDate;

public interface ClearedMarginCallGenService {

    MarginCall geneareteMarginCall(Agreement agreement, Portfolio portfolio, Valuation<MarginValuation> valuation, LocalDate date);
}
