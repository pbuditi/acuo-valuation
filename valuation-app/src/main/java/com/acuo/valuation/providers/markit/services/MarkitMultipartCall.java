package com.acuo.valuation.providers.markit.services;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.*;

public class MarkitMultipartCall extends CallBuilder<MarkitMultipartCall> {

    private final ClientEndPoint<MarkitEndPointConfig> client;
    private MultipartBody.Builder builder;
    private HttpUrl uploadUrl;

    private MarkitMultipartCall(ClientEndPoint<MarkitEndPointConfig> client) {
        this.client = client;
        MarkitEndPointConfig config = client.config();
        this.uploadUrl = new HttpUrl.Builder()
                .scheme(config.getScheme())
                .host(config.getHost())
                .port(config.getPort())
                .addPathSegment(config.getUploadPath())
                .build();
        builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username", config.getUsername())
                .addFormDataPart("password", config.getPassword());
    }

    public static MarkitMultipartCall of(ClientEndPoint<MarkitEndPointConfig> client) {
        return new MarkitMultipartCall(client);
    }

    public MarkitMultipartCall with(String key, String data) {
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data; charset=utf-8"), data);
        builder.addPart(MultipartBody.Part.createFormData(key, key, body));
        return this;
    }

    public Call create() {
        Request request = new Request.Builder().url(uploadUrl).post(builder.build()).build();
        return client.call(request, predicate);
    }
}