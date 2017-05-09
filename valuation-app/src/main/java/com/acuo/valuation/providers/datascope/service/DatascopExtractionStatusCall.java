package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class DatascopExtractionStatusCall extends CallBuilder<DatascopExtractionStatusCall> {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private Request.Builder builder;
    private HttpUrl statusUrl;

    private DatascopExtractionStatusCall(ClientEndPoint<DatascopeEndPointConfig> client) {
        this.client = client;
        builder = new Request.Builder();
    }

    public static DatascopExtractionStatusCall of(ClientEndPoint<DatascopeEndPointConfig> client) {
        return new DatascopExtractionStatusCall(client);
    }

    public DatascopExtractionStatusCall with(String key, String value) {
        if (key.equalsIgnoreCase("token")) {
            builder.header("Authorization", "Token " + value);
        } else if (key.equalsIgnoreCase("id")) {
            DatascopeEndPointConfig config = client.config();
            statusUrl = new HttpUrl.Builder()
                    .scheme(config.getScheme())
                    .host(config.getHost())
                    .port(config.getPort())
                    .addPathSegments(config.getStatuspath().replace("<id>", value))
                    .build();
            builder.header("Prefer", "respond-async")
                    .header("Content-Type", "application/json")
                    .url(statusUrl);
        }
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
