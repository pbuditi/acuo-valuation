package com.acuo.valuation.providers.datascope.service.scheduled;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.valuation.providers.datascope.service.DataScopeEndPointConfig;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DataScopeScheduleCall extends CallBuilder<DataScopeScheduleCall> {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final ClientEndPoint<DataScopeEndPointConfig> client;
    private final Request.Builder builder;

    private DataScopeScheduleCall(ClientEndPoint<DataScopeEndPointConfig> client)
    {
        this.client = client;
        DataScopeEndPointConfig config = client.config();
        HttpUrl scheduleUrl = new HttpUrl.Builder()
                .scheme(config.getScheme())
                .host(config.getHost())
                .port(config.getPort())
                .addPathSegments(config.getSchedulepath())
                .build();
        builder = new Request.Builder()
                .header("Prefer", "respond-async")
                .header("Content-Type", "application/json")
                .url(scheduleUrl);

    }

    public static DataScopeScheduleCall of(ClientEndPoint<DataScopeEndPointConfig> client) {
        return new DataScopeScheduleCall(client);
    }

    public DataScopeScheduleCall with(String key, String value) {
        if(key.equalsIgnoreCase("token"))
        {
            builder.header("Authorization", "Token " + value);
        }
        else
        if(key.equalsIgnoreCase("body"))
        {
            RequestBody body = RequestBody.create(JSON, value);
            builder.post(body);
        }
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }
}
