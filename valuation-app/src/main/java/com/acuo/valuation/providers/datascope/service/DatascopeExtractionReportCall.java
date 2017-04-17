package com.acuo.valuation.providers.datascope.service;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class DatascopeExtractionReportCall extends CallBuilder<DatascopeExtractionReportCall> {

    private final ClientEndPoint<DatascopeEndPointConfig> client;
    private Request.Builder builder;
    private HttpUrl reportUrl;

    private DatascopeExtractionReportCall(ClientEndPoint<DatascopeEndPointConfig> client)
    {
        this.client = client;
        builder = new Request.Builder();
    }

    public static DatascopeExtractionReportCall of(ClientEndPoint<DatascopeEndPointConfig> client) {
        return new DatascopeExtractionReportCall(client);
    }

    public DatascopeExtractionReportCall with(String key, String value) {
        if(key.equalsIgnoreCase("token"))
        {
            builder.header("Authorization", "Token " + value);
        }
        else
        if(key.equalsIgnoreCase("id"))
        {
            DatascopeEndPointConfig config = client.config();
            reportUrl = new HttpUrl.Builder()
                    .scheme(config.getScheme())
                    .host(config.getHost())
                    .port(config.getPort())
                    .addPathSegments(config.getReportpath().replace("<id>", value))
                    .build();
            builder.header("Prefer", "respond-async")
                    .header("Content-Type", "application/json")
                    .url(reportUrl);
        }
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
