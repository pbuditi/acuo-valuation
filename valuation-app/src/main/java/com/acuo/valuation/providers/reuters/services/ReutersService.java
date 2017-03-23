package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.Assets;

import java.util.List;

public interface ReutersService {

    List<Assets> send(Assets assets);

    public void valuate(String assetId);
}
