package com.acuo.valuation.markit.services;

import com.acuo.valuation.services.ClientCall;
import com.acuo.valuation.services.ClientCallBuilder;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.EndPointConfig;
import okhttp3.FormBody;
import okhttp3.Request;

public class MarkitFormCall extends ClientCallBuilder<MarkitFormCall> {

    private final ClientEndPoint client;
    private final EndPointConfig config;
    private FormBody.Builder builder;

    private MarkitFormCall(ClientEndPoint client) {
        this.client = client;
        this.config = client.config();
        builder = new FormBody.Builder()
                .add("username", config.username())
                .add("password", config.password());
    }

    public static MarkitFormCall of(ClientEndPoint client) {
        return new MarkitFormCall(client);
    }

    public MarkitFormCall with(String key, String value) {
        builder.add(key, value);
        return this;
    }

    public ClientCall create() {
        Request request = new Request.Builder().url(config.url()).post(builder.build()).build();
        return client.call(request, predicate);
    }
}