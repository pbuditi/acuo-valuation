package com.acuo.valuation.modules;

import com.acuo.valuation.markit.services.*;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.OkHttpClient;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.utils.LoggingInterceptor;
import com.google.inject.AbstractModule;

public class ServicesModule extends AbstractModule {


    private okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

    @Override
    protected void configure() {
        bind(okhttp3.OkHttpClient.class).toInstance(httpClient);
        bind(ClientEndPoint.class).to(OkHttpClient.class);
        bind(Sender.class).to(PortfolioValuationsSender.class);
        bind(Retriever.class).to(PortfolioValuationsRetriever.class);
        bind(PricingService.class).to(MarkitPricingService.class);
    }

}
