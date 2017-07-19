package com.acuo.valuation.modules;

import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.PortfolioResults;
import com.acuo.valuation.providers.acuo.ClarusValuationProcessor;
import com.acuo.valuation.providers.acuo.MarkitValuationProcessor;
import com.acuo.valuation.providers.acuo.PortfolioValuationProcessor;
import com.acuo.valuation.providers.acuo.assets.AssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.CashAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.ReutersAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.calls.CallGenerator;
import com.acuo.valuation.providers.acuo.calls.CallGeneratorProcessor;
import com.acuo.valuation.providers.acuo.calls.CallSimulator;
import com.acuo.valuation.providers.acuo.calls.Simulator;
import com.acuo.valuation.providers.acuo.portfolios.PortfolioManagerImpl;
import com.acuo.valuation.providers.acuo.results.MarginResultPersister;
import com.acuo.valuation.providers.acuo.results.MarkitResultPersister;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.providers.acuo.trades.ClarusIMProcessorImpl;
import com.acuo.valuation.providers.acuo.trades.ClarusPricingProcessor;
import com.acuo.valuation.providers.acuo.trades.ClarusVMProcessorImpl;
import com.acuo.valuation.providers.acuo.trades.LocalTradeCacheService;
import com.acuo.valuation.providers.acuo.trades.MarkitPricingProcessor;
import com.acuo.valuation.providers.acuo.trades.PortfolioPriceProcessor;
import com.acuo.valuation.providers.acuo.trades.PortfolioValuationPersister;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
import com.acuo.valuation.providers.acuo.trades.TradeUploadServiceTransformer;
import com.acuo.valuation.providers.clarus.services.ClarusMarginService;
import com.acuo.valuation.providers.clarus.services.ClarusMarginServiceImpl;
import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthService;
import com.acuo.valuation.providers.datascope.service.authentication.DataScopeAuthServiceImpl;
import com.acuo.valuation.providers.datascope.service.intraday.DataScopeIntradayService;
import com.acuo.valuation.providers.datascope.service.intraday.DataScopeIntradayServiceImpl;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeDownloadService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeDownloadServiceImpl;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeExtractionService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeExtractionServiceImpl;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopePersistService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopePersistServiceImpl;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeScheduleService;
import com.acuo.valuation.providers.datascope.service.scheduled.DataScopeScheduleServiceImpl;
import com.acuo.valuation.providers.markit.services.MarkitPricingService;
import com.acuo.valuation.providers.markit.services.PortfolioValuationsRetriever;
import com.acuo.valuation.providers.markit.services.PortfolioValuationsSender;
import com.acuo.valuation.providers.markit.services.Retriever;
import com.acuo.valuation.providers.markit.services.Sender;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.providers.reuters.services.ReutersServiceImpl;
import com.acuo.valuation.providers.reuters.services.SettlementDateService;
import com.acuo.valuation.providers.reuters.services.SettlementDateServiceImpl;
import com.acuo.valuation.services.PortfolioManager;
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
        bind(TradeUploadService.class).to(TradeUploadServiceTransformer.class);
        bind(TradeCacheService.class).to(LocalTradeCacheService.class);

        // markit trade valuation and margin call generation
        bind(Sender.class).to(PortfolioValuationsSender.class);
        bind(Retriever.class).to(PortfolioValuationsRetriever.class);
        bind(PricingService.class).to(MarkitPricingService.class);
        bind(Simulator.class);
        bind(new TypeLiteral<ResultPersister<MarkitResults>>(){}).to(MarkitResultPersister.class);
        bind(new TypeLiteral<ResultPersister<MarginResults>>(){}).to(MarginResultPersister.class);
        bind(new TypeLiteral<ResultPersister<PortfolioResults>>(){}).to(PortfolioValuationPersister.class);
        bind(CallGenerator.class);
        bind(CallSimulator.class);
        bind(MarkitValuationProcessor.class);
        bind(MarkitPricingProcessor.class);

        // clarus portfolio valuation and margin call generation
        bind(ClarusMarginService.class).to(ClarusMarginServiceImpl.class);
        bind(new TypeLiteral<ResultPersister<MarginResults>>(){}).to(MarginResultPersister.class);
        bind(MarginResultPersister.class);
        bind(CallSimulator.class);
        bind(ClarusValuationProcessor.class);
        bind(ClarusVMProcessorImpl.class);
        bind(ClarusIMProcessorImpl.class);
        bind(PortfolioPriceProcessor.class);

        //portfolio generation
        bind(PortfolioManager.class).to(PortfolioManagerImpl.class);
        bind(PortfolioValuationProcessor.class);

        // asset valuation
        bind(ReutersService.class).to(ReutersServiceImpl.class);
        bind(SettlementDateService.class).to(SettlementDateServiceImpl.class);
        bind(ReutersAssetPricingProcessor.class);
        bind(CashAssetPricingProcessor.class);

        // datascope
        bind(DataScopeAuthService.class).to(DataScopeAuthServiceImpl.class);
        bind(DataScopeScheduleService.class).to(DataScopeScheduleServiceImpl.class);
        bind(DataScopeExtractionService.class).to(DataScopeExtractionServiceImpl.class);
        bind(DataScopeDownloadService.class).to(DataScopeDownloadServiceImpl.class);
        bind(DataScopePersistService.class).to(DataScopePersistServiceImpl.class);
        bind(DataScopeIntradayService.class).to(DataScopeIntradayServiceImpl.class);

    }

    @Provides
    @Singleton
    CallGeneratorProcessor callGeneratorProcessor(Injector injector) {
        CallGenerator generator = injector.getInstance(CallGenerator.class);
        CallSimulator simulator = injector.getInstance(CallSimulator.class);
        generator.setNext(simulator);
        return generator;
    }

    @Provides
    @Singleton
    AssetPricingProcessor assetPricingProcessor(Injector injector) {
        ReutersAssetPricingProcessor reutersPricingProcessor = injector.getInstance(ReutersAssetPricingProcessor.class);
        CashAssetPricingProcessor cashAssetPricingProcessor = injector.getInstance(CashAssetPricingProcessor.class);
        reutersPricingProcessor.setNext(cashAssetPricingProcessor);
        return reutersPricingProcessor;
    }

    @Provides
    @Singleton
    TradePricingProcessor tradePricingProcessor(Injector injector) {
        MarkitPricingProcessor markitPricingProcessor = injector.getInstance(MarkitPricingProcessor.class);
        ClarusPricingProcessor vmProcessor = injector.getInstance(ClarusVMProcessorImpl.class);
        ClarusPricingProcessor imProcessor = injector.getInstance(ClarusIMProcessorImpl.class);
        markitPricingProcessor.setNext(vmProcessor);
        vmProcessor.setNext(imProcessor);
        return markitPricingProcessor;
    }
}