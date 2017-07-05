package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetSettlementDate;

import java.util.List;

public interface SettlementDateService {

    List<AssetSettlementDate> send(List<Assets> assets);
}
