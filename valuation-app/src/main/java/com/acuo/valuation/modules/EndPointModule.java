package com.acuo.valuation.modules;

import com.acuo.common.http.client.ClientEndPoint;
import com.acuo.common.http.client.LoggingInterceptor;
import com.acuo.valuation.providers.clarus.services.ClarusClient;
import com.acuo.valuation.providers.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.providers.markit.services.MarkitClient;
import com.acuo.valuation.providers.markit.services.MarkitEndPointConfig;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import java.util.concurrent.TimeUnit;

public class EndPointModule extends AbstractModule {

    @Override
    protected void configure() {
        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().connectTimeout(0, TimeUnit.MILLISECONDS).addInterceptor(new LoggingInterceptor()).build();
        bind(okhttp3.OkHttpClient.class).toInstance(httpClient);
        bind(new TypeLiteral<ClientEndPoint<MarkitEndPointConfig>>(){}).to(MarkitClient.class);
        bind(new TypeLiteral<ClientEndPoint<ClarusEndPointConfig>>(){}).to(ClarusClient.class);
    }

}