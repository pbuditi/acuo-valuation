package com.acuo.valuation.services;

import com.acuo.persist.entity.Portfolio;
import com.acuo.valuation.jackson.MarginCallResponse;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioManager {

    List<Portfolio> valueMissing(List<String> portfolioIds, LocalDate valuationDate);

    MarginCallResponse split(List<Portfolio> portfolios, LocalDate valuationDate);

}
