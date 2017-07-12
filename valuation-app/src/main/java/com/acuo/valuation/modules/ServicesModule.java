package com.acuo.valuation.modules;

import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.PortfolioResults;
import com.acuo.valuation.providers.acuo.assets.AssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.CashAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.assets.ReutersAssetPricingProcessor;
import com.acuo.valuation.providers.acuo.calls.CallGeneratorProcessor;
import com.acuo.valuation.providers.acuo.calls.ClarusCallGenerator;
import com.acuo.valuation.providers.acuo.calls.ClarusCallSimulator;
import com.acuo.valuation.providers.acuo.calls.MarkitCallGenerator;
import com.acuo.valuation.providers.acuo.calls.MarkitCallSimulator;
import com.acuo.valuation.providers.acuo.calls.Simulator;
import com.acuo.valuation.providers.acuo.ClarusValuationProcessor;
import com.acuo.valuation.providers.acuo.results.MarginResultPersister;
import com.acuo.valuation.providers.acuo.results.MarkitResultPersister;
import com.acuo.valuation.providers.acuo.MarkitValuationProcessor;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.providers.acuo.trades.*;
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
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.services.TradeCacheService;
import com.acuo.valuation.services.TradeUploadService;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import javax.inject.Named;
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
        bind(MarkitCallGenerator.class);
        bind(MarkitCallSimulator.class);
        bind(MarkitValuationProcessor.class);
        bind(MarkitPricingProcessor.class);

        // clarus portfolio valuation and margin call generation
        bind(ClarusMarginService.class).to(ClarusMarginServiceImpl.class);
        bind(new TypeLiteral<ResultPersister<MarginResults>>(){}).to(MarginResultPersister.class);
        bind(MarginResultPersister.class);
        bind(ClarusCallGenerator.class);
        bind(ClarusCallSimulator.class);
        bind(ClarusValuationProcessor.class);
        bind(ClarusVMProcessorImpl.class);
        bind(ClarusIMProcessorImpl.class);
        bind(PortfolioPriceProcessor.class);

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
    @Named("markit")
    CallGeneratorProcessor markitCallGeneratorProcessor(Injector injector) {
        MarkitCallGenerator markitProcessor = injector.getInstance(MarkitCallGenerator.class);
        MarkitCallSimulator simulator = injector.getInstance(MarkitCallSimulator.class);
        markitProcessor.setNext(simulator);
        return markitProcessor;
    }

    @Provides
    @Singleton
    @Named("clarus")
    CallGeneratorProcessor clarusCallGeneratorProcessor(Injector injector) {
        ClarusCallGenerator clarusProcessor = injector.getInstance(ClarusCallGenerator.class);
        ClarusCallSimulator simulator = injector.getInstance(ClarusCallSimulator.class);
        clarusProcessor.setNext(simulator);
        return clarusProcessor;
    }

    @Provides
    @Singleton
    @Named("portfolio")
    CallGeneratorProcessor portfolioCallGeneratorProcessor(Injector injector) {
        MarkitCallGenerator markitProcessor = injector.getInstance(MarkitCallGenerator.class);
        MarkitCallSimulator markitSimulator = injector.getInstance(MarkitCallSimulator.class);
        ClarusCallGenerator clarusProcessor = injector.getInstance(ClarusCallGenerator.class);
        ClarusCallSimulator clarussimulator = injector.getInstance(ClarusCallSimulator.class);

        markitProcessor.setNext(markitSimulator);
        markitSimulator.setNext(clarusProcessor);
        clarusProcessor.setNext(clarussimulator);
        return markitProcessor;
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