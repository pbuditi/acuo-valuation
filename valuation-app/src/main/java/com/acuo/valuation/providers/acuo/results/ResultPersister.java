package com.acuo.valuation.providers.acuo.results;

import com.acuo.common.model.ids.PortfolioId;

import java.util.Set;

public interface ResultPersister<T> {

    Set<PortfolioId> persist(T results);
}
