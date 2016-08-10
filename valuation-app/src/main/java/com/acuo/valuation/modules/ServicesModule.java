package com.acuo.valuation.modules;

import com.acuo.valuation.clarus.services.ClarusMarginCalcService;
import com.acuo.valuation.markit.services.*;
import com.acuo.valuation.services.MarginCalcService;
import com.acuo.valuation.services.PricingService;
import com.google.inject.AbstractModule;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Sender.class).to(PortfolioValuationsSender.class);
        bind(Retriever.class).to(PortfolioValuationsRetriever.class);
        bind(PricingService.class).to(MarkitPricingService.class);
        bind(MarginCalcService.class).to(ClarusMarginCalcService.class);
    }

}
