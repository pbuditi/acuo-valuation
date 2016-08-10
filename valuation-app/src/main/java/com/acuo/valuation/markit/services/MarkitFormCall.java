package com.acuo.valuation.markit.services;

import com.acuo.valuation.services.Call;
import com.acuo.valuation.services.CallBuilder;
import com.acuo.valuation.services.ClientEndPoint;
import okhttp3.FormBody;
import okhttp3.Request;

public class MarkitFormCall extends CallBuilder<MarkitFormCall> {

    private final ClientEndPoint<MarkitEndPointConfig> client;
    private final MarkitEndPointConfig config;
    private FormBody.Builder builder;

    private MarkitFormCall(ClientEndPoint<MarkitEndPointConfig> client) {
        this.client = client;
        this.config = client.config();
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
        Request request = new Request.Builder().url(config.getUrl()).post(builder.build()).build();
        return client.call(request, predicate);
    }
}