package com.acuo.valuation.providers.reuters.services;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ReutersCall extends CallBuilder<ReutersCall> {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final ClientEndPoint<ReutersEndPointConfig> client;

    private final Request.Builder builder;

    private ReutersCall(ClientEndPoint<ReutersEndPointConfig> client) {
        this.client = client;
        ReutersEndPointConfig config = client.config();
        HttpUrl uploadUrl = new HttpUrl.Builder()
                .scheme(config.getScheme())
                .host(config.getHost())
                .port(config.getPort())
                .addPathSegments(config.getUploadPath())
                .addQueryParameter("apikey", config.getApikey())
                .build();
        builder = new Request.Builder()
                .header("Content-Type", "application/json")
                .header("X-TRACS-Position", config.getHeaderPosition())
                .header("X-TRACS-ApplicationId", config.getHeaderApplicationId())
                .url(uploadUrl);
    }

    public static ReutersCall of(ClientEndPoint<ReutersEndPointConfig> client) {
        return new ReutersCall(client);
    }

    public ReutersCall with(String key, String value) {
        RequestBody body = RequestBody.create(JSON, value);
        builder.post(body);
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
