package com.acuo.valuation.services;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.ids.PortfolioId;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface CallService {

    List<String> createCalls(Set<PortfolioId> portfolioSet, LocalDate valuationDate, LocalDate callDate, Types.CallType callType);

}
