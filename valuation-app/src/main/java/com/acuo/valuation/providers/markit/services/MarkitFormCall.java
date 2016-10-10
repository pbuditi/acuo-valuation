package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class MarkitFormCall extends CallBuilder<MarkitFormCall> {

    private final ClientEndPoint<MarkitEndPointConfig> client;
    private final HttpUrl downloadUrl;
    private FormBody.Builder builder;

    private MarkitFormCall(ClientEndPoint<MarkitEndPointConfig> client) {
        this.client = client;
        MarkitEndPointConfig config = client.config();
        this.downloadUrl = new HttpUrl.Builder()
                .scheme(config.getScheme())
                .host(config.getHost())
                .port(config.getPort())
                .addPathSegment(config.getDownloadPath())
                .build();
        builder = new FormBody.Builder()
                .add("username", config.getUsername())
                .add("password", config.getPassword());
    }

    public static MarkitFormCall of(ClientEndPoint<MarkitEndPointConfig> client) {
        return new MarkitFormCall(client);
    }

    public MarkitFormCall with(String key, String value) {
        builder.add(key, value);
        return this;
    }

    public Call create() {
        Request request = new Request.Builder().url(downloadUrl).post(builder.build()).build();
        return client.call(request, predicate);
    }
}