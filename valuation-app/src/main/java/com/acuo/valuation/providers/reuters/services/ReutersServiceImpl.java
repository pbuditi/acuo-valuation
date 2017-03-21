package com.acuo.valuation.providers.reuters.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.TransformerContext;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.model.assets.Assets;
import com.acuo.valuation.services.ReutersService;

import javax.inject.Inject;
import javax.inject.Named;

public class ReutersServiceImpl implements ReutersService {

    private final ClientEndPoint<ReutersEndPointConfig> client;
    private final Transformer<Assets> transformer;

    @Inject
    public ReutersServiceImpl(ClientEndPoint<ReutersEndPointConfig> client, @Named("assets") Transformer<Assets> transformer)
    {
        this.client = client;
        this.transformer = transformer;
    }

    public String send(Assets assets)
    {
        TransformerContext context = new TransformerContext();
        String json = transformer.serialise(assets, context);
        return ReutersCall.of(client).with("josn", json).create().send();
    }
}
