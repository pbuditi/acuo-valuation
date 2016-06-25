package com.acuo.valuation.markit.services;

import com.acuo.valuation.requests.RequestBuilder;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.EndPointConfig;
import okhttp3.FormBody;
import okhttp3.Request;

public class MarkitGetRequestBuilder extends RequestBuilder<MarkitGetRequestBuilder> {

    private final ClientEndPoint client;
    private final EndPointConfig config;
    private FormBody.Builder builder;

    public MarkitGetRequestBuilder(ClientEndPoint client, EndPointConfig config) {
        this.client = client;
        this.config = config;
        builder = new FormBody.Builder()
                .add("username", config.username())
                .add("password", config.password());
    }

    public MarkitGetRequestBuilder with(String key, String value) {
        builder.add(key, value);
        return this;
    }

    public String send() {
        Request request = new Request.Builder().url(config.url()).post(builder.build()).build();
        MarkitClientCall call = new MarkitClientCall(request, predicate);
        return client.send(call);
    }
}