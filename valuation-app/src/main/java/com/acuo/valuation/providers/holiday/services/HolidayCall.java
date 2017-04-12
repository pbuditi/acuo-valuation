package com.acuo.valuation.providers.holiday.services;

import com.acuo.common.http.client.Call;
import com.acuo.common.http.client.CallBuilder;
import com.acuo.common.http.client.ClientEndPoint;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HolidayCall extends CallBuilder<HolidayCall> {

    private final ClientEndPoint<HolidayEndPointConfig> client;

    private Request.Builder builder;


    private HolidayCall(ClientEndPoint<HolidayEndPointConfig> client )
    {
        this.client = client;
    }

    public static HolidayCall of(ClientEndPoint<HolidayEndPointConfig> client)
    {
        return new HolidayCall(client);
    }

    public HolidayCall with(String key, String value) {
        HolidayEndPointConfig config = client.config();
        HttpUrl url = new HttpUrl.Builder()
                .scheme(config.getScheme())
                .host(config.getHost())
                .port(config.getPort())
                .addPathSegments(config.getPath().replace("<date>", value))
                .addQueryParameter("apikey", config.getApikey())
                .build();
        builder = new Request.Builder().url(url);
        return this;
    }

    public Call create() {
        Request request = builder.build();
        return client.call(request, predicate);
    }

}
