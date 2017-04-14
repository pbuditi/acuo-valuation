package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.http.client.OkHttpClient;

import javax.inject.Inject;

public class ReutersClient extends OkHttpClient<ReutersEndPointConfig> {

    @Inject
    public ReutersClient(okhttp3.OkHttpClient httpClient, ReutersEndPointConfig config) {
        super(httpClient, config);
    }
}
