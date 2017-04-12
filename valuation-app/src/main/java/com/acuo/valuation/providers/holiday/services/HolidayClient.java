package com.acuo.valuation.providers.holiday.services;

import com.acuo.common.http.client.OkHttpClient;

import javax.inject.Inject;

public class HolidayClient extends OkHttpClient<HolidayEndPointConfig> {

    @Inject
    public HolidayClient(okhttp3.OkHttpClient httpClient, HolidayEndPointConfig config) {
        super(httpClient, config);
    }
}
