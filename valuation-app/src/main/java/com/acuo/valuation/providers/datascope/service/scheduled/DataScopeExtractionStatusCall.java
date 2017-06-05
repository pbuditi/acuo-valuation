package com.acuo.valuation.providers.datascope.service.scheduled;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class DataScopeExtractionStatusCall extends CallBuilder<DataScopeExtractionStatusCall> {

    private final ClientEndPoint<DataScopeEndPointConfig> client;
    private Request.Builder builder;
    private HttpUrl statusUrl;

    private DataScopeExtractionStatusCall(ClientEndPoint<DataScopeEndPointConfig> client) {
        this.client = client;
        builder = new Request.Builder();
    }

    public static DataScopeExtractionStatusCall of(ClientEndPoint<DataScopeEndPointConfig> client) {
        return new DataScopeExtractionStatusCall(client);
    }

    public DataScopeExtractionStatusCall with(String key, String value) {
        if (key.equalsIgnoreCase("token")) {
            builder.header("Authorization", "Token " + value);
        } else if (key.equalsIgnoreCase("id")) {
            DataScopeEndPointConfig config = client.config();
            statusUrl = new HttpUrl.Builder()
                    .scheme(config.getScheme())
                    .host(config.getHost())
                    .port(config.getPort())
                    .addPathSegments(config.getStatuspath().replace("<id>", value))
                    .build();
            builder.header("Prefer", "respond-async")
                    .header("Content-Type", "application/json")
                    .url(statusUrl);
        }
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
