package com.acuo.valuation.providers.datascope.service.authentication;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DataScopeAuthCall extends CallBuilder<DataScopeAuthCall> {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final ClientEndPoint<DataScopeEndPointConfig> client;
    private final Request.Builder builder;
    private final HttpUrl authUrl;

    private DataScopeAuthCall(ClientEndPoint<DataScopeEndPointConfig> client) {
        this.client = client;
        DataScopeEndPointConfig config = client.config();
        authUrl = new HttpUrl.Builder()
                .scheme(config.getScheme())
                .host(config.getHost())
                .port(config.getPort())
                .addPathSegments(config.getAuthpath())
                .build();
        builder = new Request.Builder()
                .header("Content-Type", "application/json")
                .url(authUrl);
    }

    public static DataScopeAuthCall of(ClientEndPoint<DataScopeEndPointConfig> client) {
        return new DataScopeAuthCall(client);
    }

    public DataScopeAuthCall with(String key, String value) {
        RequestBody body = RequestBody.create(JSON, value);
        builder.post(body);
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
