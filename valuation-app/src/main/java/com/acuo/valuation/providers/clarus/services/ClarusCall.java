package com.acuo.valuation.providers.clarus.services;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

@Slf4j
public class ClarusCall extends CallBuilder<ClarusCall> {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final ClientEndPoint<ClarusEndPointConfig> client;
    private final ClarusEndPointConfig config;
    private final Request.Builder builder;

    private ClarusCall(ClientEndPoint<ClarusEndPointConfig> client, MarginCallType callType) {
        this.client = client;
        this.config = client.config();
        String credential = Credentials.basic(config.getKey(), config.getSecret());
        builder = new Request.Builder()
                .header("Content-Type", "application/json")
                .header("Authorization", credential)
                .url(config.getHost() + callType.name() + ".json");
    }

    public static ClarusCall of(ClientEndPoint<ClarusEndPointConfig> client, MarginCallType callType) {
        return new ClarusCall(client, callType);
    }

    @Override
    public ClarusCall with(String key, String value) {
        RequestBody body = RequestBody.create(JSON, value);
        builder.post(body);
        return this;
    }

    @Override
    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
