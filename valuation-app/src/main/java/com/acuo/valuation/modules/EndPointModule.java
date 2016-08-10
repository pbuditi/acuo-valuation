package com.acuo.valuation.modules;

import com.acuo.valuation.clarus.services.ClarusClient;
import com.acuo.valuation.clarus.services.ClarusEndPointConfig;
import com.acuo.valuation.markit.services.MarkitClient;
import com.acuo.valuation.markit.services.MarkitEndPointConfig;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.utils.LoggingInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class EndPointModule extends AbstractModule {

    @Override
    protected void configure() {
        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();
        bind(okhttp3.OkHttpClient.class).toInstance(httpClient);
        bind(new TypeLiteral<ClientEndPoint<MarkitEndPointConfig>>(){}).to(MarkitClient.class);
        bind(new TypeLiteral<ClientEndPoint<ClarusEndPointConfig>>(){}).to(ClarusClient.class);
    }

}