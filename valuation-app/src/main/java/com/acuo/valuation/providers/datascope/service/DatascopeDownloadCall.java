package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class DatascopeDownloadCall extends CallBuilder<DatascopeDownloadCall> {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private Request.Builder builder;
    private HttpUrl downloadUrl;

    private DatascopeDownloadCall(ClientEndPoint<DatascopeEndPointConfig> client)
    {
        this.client = client;
        builder = new Request.Builder();
    }

    public static DatascopeDownloadCall of(ClientEndPoint<DatascopeEndPointConfig> client) {
        return new DatascopeDownloadCall(client);
    }

    public DatascopeDownloadCall with(String key, String value) {
        if(key.equalsIgnoreCase("token"))
        {
            builder.header("Authorization", "Token " + value);
        }
        else
        if(key.equalsIgnoreCase("id"))
        {
            DatascopeEndPointConfig config = client.config();
            downloadUrl = new HttpUrl.Builder()
                    .scheme(config.getScheme())
                    .host(config.getHost())
                    .port(config.getPort())
                    .addPathSegments(config.getDownloadpath().replace("<id>", value))
                    .build();
            builder.header("Prefer", "respond-async")
                    .header("Content-Type", "application/json")
                    .url(downloadUrl);
        }
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}