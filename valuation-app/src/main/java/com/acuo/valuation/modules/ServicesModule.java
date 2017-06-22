package com.acuo.valuation.modules;

import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.PortfolioResults;
import com.acuo.valuation.providers.acuo.assets.AssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.CashAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.ReutersAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.calls.ClarusCallGenerator;
import com.acuo.valuation.providers.acuo.calls.ClarusCallSimulator;
import com.acuo.valuation.providers.acuo.calls.MarkitCallGenerator;
import com.acuo.valuation.providers.acuo.calls.MarkitCallSimulator;
import com.acuo.valuation.providers.acuo.calls.Simulator;
import com.acuo.valuation.providers.acuo.results.ClarusValuationProcessor;
import com.acuo.valuation.providers.acuo.results.MarginResultPersister;
import com.acuo.valuation.providers.acuo.results.MarkitResultPersister;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.providers.acuo.results.ResultProcessor;
import com.acuo.valuation.providers.acuo.trades.ClarusIMProcessorImpl;
import com.acuo.valuation.providers.acuo.trades.ClarusPricingProcessor;
import com.acuo.valuation.providers.acuo.trades.ClarusVMProcessorImpl;
import com.acuo.valuation.providers.acuo.trades.LocalTradeCacheService;
import com.acuo.valuation.providers.acuo.trades.MarkitPricingProcessor;
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
        bind(new TypeLiteral<ResultPersister<MarkitResults>>(){}).to(MarkitResultPersister.class);
        bind(Simulator.class);
        bind(MarkitResultPersister.class);
        bind(MarkitCallGenerator.class);
        bind(MarkitCallSimulator.class);
        bind(MarkitValuationProcessor.class);
        bind(MarkitResultPersister.class);
        bind(MarkitPricingProcessor.class);
        bind(new TypeLiteral<ResultPersister<PortfolioResults>>(){}).to(PortfolioValuationPersister.class);

        // clarus portfolio valuation and margin call generation
        bind(ClarusMarginService.class).to(ClarusMarginServiceImpl.class);
        bind(new TypeLiteral<ResultPersister<MarginResults>>(){}).to(MarginResultPersister.class);
        bind(MarginResultPersister.class);
        bind(ClarusCallGenerator.class);
        bind(ClarusCallSimulator.class);
        bind(ClarusValuationProcessor.class);
        bind(ClarusVMProcessorImpl.class);
        bind(ClarusIMProcessorImpl.class);

        // asset valuation
        bind(ReutersService.class).to(ReutersServiceImpl.class);
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
    ResultProcessor<MarkitResults> markitResultProcessor(Injector injector) {
        MarkitResultPersister resultPersister = injector.getInstance(MarkitResultPersister.class);
        MarkitCallGenerator markitProcessor = injector.getInstance(MarkitCallGenerator.class);
        MarkitCallSimulator simulator = injector.getInstance(MarkitCallSimulator.class);
        resultPersister.setNext(markitProcessor);
        markitProcessor.setNext(simulator);
        return resultPersister;
    }

    @Provides
    @Singleton
    ResultProcessor<MarginResults> clarusResultProcessor(Injector injector) {
        MarginResultPersister resultPersister = injector.getInstance(MarginResultPersister.class);
        ClarusCallGenerator clarusProcessor = injector.getInstance(ClarusCallGenerator.class);
        ClarusCallSimulator simulator = injector.getInstance(ClarusCallSimulator.class);
        resultPersister.setNext(clarusProcessor);
        clarusProcessor.setNext(simulator);
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