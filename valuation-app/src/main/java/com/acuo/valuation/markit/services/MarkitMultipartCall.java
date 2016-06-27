package com.acuo.valuation.markit.services;

import com.acuo.valuation.services.ClientCall;
import com.acuo.valuation.services.ClientCallBuilder;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.EndPointConfig;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MarkitMultipartCall extends ClientCallBuilder<MarkitMultipartCall> {

    private final ClientEndPoint client;
    private final EndPointConfig config;
    private MultipartBody.Builder builder;

    private MarkitMultipartCall(ClientEndPoint client) {
        this.client = client;
        this.config = client.config();
        builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username", config.username())
                .addFormDataPart("password", config.password());
    }

    public static MarkitMultipartCall of(ClientEndPoint client) {
        return new MarkitMultipartCall(client);
    }

    public MarkitMultipartCall with(String key, String data) {
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), data);
        builder.addPart(MultipartBody.Part.createFormData(key, key, body));
        return this;
    }

    public ClientCall create() {
        Request request = new Request.Builder().url(config.url()).post(builder.build()).build();
        return client.call(request, predicate);
    }
}