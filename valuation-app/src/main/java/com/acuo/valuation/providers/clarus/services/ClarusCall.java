package com.acuo.valuation.providers.clarus.services;

import com.acuo.valuation.services.Call;
import com.acuo.valuation.services.CallBuilder;
import com.acuo.valuation.services.ClientEndPoint;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClarusCall extends CallBuilder<ClarusCall> {

    private static final Logger LOG = LoggerFactory.getLogger(ClarusCall.class);

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final ClientEndPoint<ClarusEndPointConfig> client;
    private final ClarusEndPointConfig config;
    private Request.Builder builder;

    private ClarusCall(ClientEndPoint<ClarusEndPointConfig> client) {
        this.client = client;
        this.config = client.config();
        String credential = Credentials.basic(config.getKey(), config.getSecret());
        builder = new Request.Builder()
                .header("Content-Type", "application/json")
                .header("Authorization", credential)
                .url(config.getHost() + "MarginCalc.json");
    }

    public static ClarusCall of(ClientEndPoint<ClarusEndPointConfig> client) {
        return new ClarusCall(client);
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

    private static String bodyToString(final Request request){
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
