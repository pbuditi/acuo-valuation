package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.OkHttpClient;

import javax.inject.Inject;

public class DatascopeClient extends OkHttpClient<DatascopeEndPointConfig> {

    @Inject
    public DatascopeClient(okhttp3.OkHttpClient httpClient, DatascopeEndPointConfig config) {
        super(httpClient, config);
    }

}
