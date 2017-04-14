package com.acuo.valuation.services;

import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.ids.PortfolioId;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MarginCallGenService {

    List<VariationMargin> createCalls(Set<PortfolioId> portfolioSet, LocalDate valuationDate, LocalDate callDate);

}
