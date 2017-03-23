package com.acuo.valuation.modules;

import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.providers.acuo.*;
import com.acuo.valuation.providers.clarus.services.ClarusMarginCalcService;
import com.acuo.valuation.providers.markit.services.*;
import com.acuo.valuation.providers.reuters.services.AssetsPersistService;
import com.acuo.valuation.providers.reuters.services.AssetsPersistServiceImpl;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.providers.reuters.services.ReutersServiceImpl;
import com.acuo.valuation.quartz.AcuoJobFactory;
import com.acuo.valuation.services.*;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import org.quartz.spi.JobFactory;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Sender.class).to(PortfolioValuationsSender.class);
        bind(Retriever.class).to(PortfolioValuationsRetriever.class);
        bind(PricingService.class).to(MarkitPricingService.class);
        bind(new TypeLiteral<ResultPersister<PricingResults>>(){}).to(PricingResultPersister.class);
        bind(new TypeLiteral<ResultPersister<MarginResults>>(){}).to(MarginResultPersister.class);
        bind(MarginCallGenService.class).to(MarkitMarginCallGenerator.class);
        bind(MarkitValautionsProcessor.class);
        bind(MarginCalcService.class).to(ClarusMarginCalcService.class);
        bind(TradeUploadService.class).to(TradeUploadServiceImpl.class);
        bind(JobFactory.class).to(AcuoJobFactory.class);
        bind(ReutersService.class).to(ReutersServiceImpl.class);
        bind(AssetsPersistService.class).to(AssetsPersistServiceImpl.class);
    }

}
