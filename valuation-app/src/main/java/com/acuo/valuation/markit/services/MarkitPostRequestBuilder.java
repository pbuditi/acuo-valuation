package com.acuo.valuation.markit.services;

import com.acuo.valuation.requests.RequestBuilder;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.EndPointConfig;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MarkitPostRequestBuilder extends RequestBuilder<MarkitPostRequestBuilder> {

    private final ClientEndPoint client;
    private final EndPointConfig config;
    private MultipartBody.Builder builder;

    public MarkitPostRequestBuilder(ClientEndPoint client, EndPointConfig config) {
        this.client = client;
        this.config = config;
        builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username", config.username())
                .addFormDataPart("password", config.password());
    }

    public MarkitPostRequestBuilder with(String key, String data) {
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), data);
        builder.addPart(MultipartBody.Part.createFormData(key, key, body));
        return this;
    }

    public String send() {
        Request request = new Request.Builder().url(config.url()).post(builder.build()).build();
        MarkitClientCall call = new MarkitClientCall(request, predicate);
        return client.send(call);
    }
}