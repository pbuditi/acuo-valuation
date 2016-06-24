package com.acuo.valuation.modules;

import com.acuo.valuation.markit.services.*;
import com.acuo.valuation.services.ClientEndPoint;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.utils.LoggingInterceptor;
import com.google.inject.AbstractModule;
import okhttp3.OkHttpClient;

public class ServicesModule extends AbstractModule {



	private OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

	@Override
	protected void configure() {
		bind(OkHttpClient.class).toInstance(httpClient);
		bind(ClientEndPoint.class).to(MarkitClient.class);
		bind(Sender.class).to(PortfolioValuationsSender.class);
		bind(Retriever.class).to(PortfolioValuationsRetriever.class);
		bind(PricingService.class).to(MarkitPricingService.class);
	}

}
