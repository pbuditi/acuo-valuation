package com.acuo.valuation.providers.datascope.service.scheduled;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import okhttp3.HttpUrl;
import okhttp3.Request;

public class DataScopeExtractionReportCall extends CallBuilder<DataScopeExtractionReportCall> {

    private final ClientEndPoint<DataScopeEndPointConfig> client;
    private Request.Builder builder;
    private HttpUrl reportUrl;

    private DataScopeExtractionReportCall(ClientEndPoint<DataScopeEndPointConfig> client)
    {
        this.client = client;
        builder = new Request.Builder();
    }

    public static DataScopeExtractionReportCall of(ClientEndPoint<DataScopeEndPointConfig> client) {
        return new DataScopeExtractionReportCall(client);
    }

    public DataScopeExtractionReportCall with(String key, String value) {
        if(key.equalsIgnoreCase("token"))
        {
            builder.header("Authorization", "Token " + value);
        }
        else
        if(key.equalsIgnoreCase("id"))
        {
            DataScopeEndPointConfig config = client.config();
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
