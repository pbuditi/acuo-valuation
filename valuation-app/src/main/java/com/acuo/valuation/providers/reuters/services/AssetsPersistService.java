package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.results.AssetValuation;

public interface AssetsPersistService {

    void persist(AssetValuation assets);
}
