package com.acuo.valuation.providers.clarus.services;

import com.acuo.common.http.client.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClarusClient extends OkHttpClient<ClarusEndPointConfig> {

    @Inject
    public ClarusClient(okhttp3.OkHttpClient httpClient, ClarusEndPointConfig config) {
        super(httpClient, config);
    }
}
