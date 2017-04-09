package com.acuo.valuation.services;

import com.acuo.persist.entity.*;

import java.time.LocalDate;

public interface ClearedMarginCallGenService {

    MarginCall geneareteMarginCall(Agreement agreement, Portfolio portfolio, Valuation<MarginValuation> valuation, LocalDate date);
}
