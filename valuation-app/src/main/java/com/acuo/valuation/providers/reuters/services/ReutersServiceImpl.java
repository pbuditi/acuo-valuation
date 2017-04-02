package com.acuo.valuation.providers.reuters.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.TransformerContext;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.model.assets.Assets;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.utils.AssetsBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Slf4j
public class ReutersServiceImpl implements ReutersService {

    private final ClientEndPoint<ReutersEndPointConfig> client;
    private final Transformer<Assets> transformer;
    private final AssetService assetService;
    private final AssetsPersistService assetsPersistService;

    @Inject
    public ReutersServiceImpl(ClientEndPoint<ReutersEndPointConfig> client, @Named("assets") Transformer<Assets> transformer, AssetService assetService,AssetsPersistService assetsPersistService)
    {
        this.client = client;
        this.transformer = transformer;
        this.assetService = assetService;
        this.assetsPersistService = assetsPersistService;
    }

    public List<Assets> send(Assets assets)
    {
        TransformerContext context = new TransformerContext();
        String json = transformer.serialise(assets, context);
        log.info(json);
        String response =  ReutersCall.of(client).with("josn", json).create().send();
        log.info(response);
        List<Assets> returnAssets = transformer.deserialiseToList(response.substring(1));
        return returnAssets;
    }



}
