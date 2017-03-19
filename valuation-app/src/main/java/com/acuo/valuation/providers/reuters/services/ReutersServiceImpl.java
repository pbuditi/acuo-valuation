package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.services.ReutersService;

import javax.inject.Inject;

public class ReutersServiceImpl implements ReutersService {

    private final ClientEndPoint<ReutersEndPointConfig> client;

    @Inject
    public ReutersServiceImpl(ClientEndPoint<ReutersEndPointConfig> client)
    {
        this.client = client;
    }

    public String send(String value)
    {
        return ReutersCall.of(client).with("josn", value).create().send();
    }
}
