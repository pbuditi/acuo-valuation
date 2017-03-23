package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.model.assets.Assets;
import com.acuo.persist.services.AssetService;

import javax.inject.Inject;

public class AssetsPersistServiceImpl implements AssetsPersistService {

    private final AssetService assetService;


    @Inject
    public AssetsPersistServiceImpl(AssetService assetService)
    {
        this.assetService = assetService;
    }


    public void persist(Assets assets)
    {

    }
}
