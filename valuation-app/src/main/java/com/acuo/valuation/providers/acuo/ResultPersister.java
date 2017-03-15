package com.acuo.valuation.providers.acuo;

import com.acuo.persist.ids.PortfolioId;

import java.util.Set;

public interface ResultPersister<T> {

    Set<PortfolioId> persist(T results);
}
