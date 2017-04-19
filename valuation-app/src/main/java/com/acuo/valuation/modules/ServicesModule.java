package com.acuo.valuation.modules;

import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.assets.AssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.CashAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.ReutersAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.calls.MarginResultPersister;
import com.acuo.valuation.providers.acuo.calls.MarkitMarginCallGenerator;
import com.acuo.valuation.providers.acuo.calls.SimulationMarginCallBuilder;
import com.acuo.valuation.providers.acuo.results.MarkitResultProcessor;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.providers.acuo.results.MarkitResultPersister;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.providers.acuo.trades.LocalTradeCacheService;
import com.acuo.valuation.providers.acuo.trades.TradeUploadServiceImpl;
import com.acuo.valuation.providers.clarus.services.ClarusMarginCalcService;
import com.acuo.valuation.providers.datascope.service.*;
import com.acuo.valuation.providers.markit.services.MarkitPricingService;
import com.acuo.valuation.providers.markit.services.PortfolioValuationsRetriever;
import com.acuo.valuation.providers.markit.services.PortfolioValuationsSender;
import com.acuo.valuation.providers.markit.services.Retriever;
import com.acuo.valuation.providers.markit.services.Sender;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.providers.reuters.services.ReutersServiceImpl;
import com.acuo.valuation.services.MarginCalcService;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeCacheService;
import com.acuo.valuation.services.TradeUploadService;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import javax.inject.Singleton;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {

        // common
        bind(TradeUploadService.class).to(TradeUploadServiceImpl.class);
        bind(TradeCacheService.class).to(LocalTradeCacheService.class);

        // markit trade valuation and margin call generation
        bind(Sender.class).to(PortfolioValuationsSender.class);
        bind(Retriever.class).to(PortfolioValuationsRetriever.class);
        bind(PricingService.class).to(MarkitPricingService.class);
        bind(new TypeLiteral<ResultPersister<MarkitResults>>(){}).to(MarkitResultPersister.class);
        bind(MarkitMarginCallGenerator.class);
        bind(SimulationMarginCallBuilder.class);
        bind(MarkitValuationProcessor.class);
        bind(MarkitResultPersister.class);

        // clarus portfolio valuation and margin call generation
        bind(MarginCalcService.class).to(ClarusMarginCalcService.class);
        bind(new TypeLiteral<ResultPersister<MarginResults>>(){}).to(MarginResultPersister.class);

        // asset valuation
        bind(ReutersService.class).to(ReutersServiceImpl.class);
        bind(ReutersAssetPricingProcessor.class);
        bind(CashAssetPricingProcessor.class);

        // datascope
        bind(DatascopeAuthService.class).to(DatascopeAuthServiceImpl.class);
        bind(DatascopeScheduleService.class).to(DatascopeScheduleServiceImpl.class);
        bind(DatascopeExtractionService.class).to(DatascopeExtractionServiceImpl.class);
        bind(DatascopeDownloadService.class).to(DatascopeDownloadServiceImpl.class);
        bind(DatascopePersistService.class).to(DatascopePersistServiceImpl.class);
    }

    @Provides
    @Singleton
    MarkitResultProcessor markitResultProcessor(Injector injector) {
        MarkitResultPersister resultPersister = injector.getInstance(MarkitResultPersister.class);
        MarkitMarginCallGenerator markitProcessor = injector.getInstance(MarkitMarginCallGenerator.class);
        SimulationMarginCallBuilder simulator = injector.getInstance(SimulationMarginCallBuilder.class);
        resultPersister.setNext(markitProcessor);
        markitProcessor.setNext(simulator);
        return resultPersister;
    }

    @Provides
    @Singleton
    AssetPricingProcessor assetPricingProcessor(Injector injector) {
        ReutersAssetPricingProcessor reutersPricingProcessor = injector.getInstance(ReutersAssetPricingProcessor.class);
        CashAssetPricingProcessor cashAssetPricingProcessor = injector.getInstance(CashAssetPricingProcessor.class);
        reutersPricingProcessor.setNext(cashAssetPricingProcessor);
        return reutersPricingProcessor;
    }
}