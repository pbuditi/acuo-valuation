package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DatascopeAuthCall extends CallBuilder<DatascopeAuthCall> {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private final Request.Builder builder;
    private final HttpUrl authUrl;

    private DatascopeAuthCall(ClientEndPoint<DatascopeEndPointConfig> client) {
        this.client = client;
        DatascopeEndPointConfig config = client.config();
        authUrl = new HttpUrl.Builder()
                .scheme(config.getScheme())
                .host(config.getHost())
                .port(config.getPort())
                .addPathSegments(config.getAuthpath())
                .build();
        builder = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(authUrl);
    }

    public static DatascopeAuthCall of(ClientEndPoint<DatascopeEndPointConfig> client) {
        return new DatascopeAuthCall(client);
    }

    public DatascopeAuthCall with(String key, String value) {
        RequestBody body = RequestBody.create(JSON, value);
        builder.post(body);
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
