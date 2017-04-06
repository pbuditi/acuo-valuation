package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;

import java.util.List;

public interface ReutersService {

    List<AssetValuation> send(List<Assets> assets);

}
