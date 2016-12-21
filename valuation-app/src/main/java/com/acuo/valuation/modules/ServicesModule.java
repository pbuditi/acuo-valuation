package com.acuo.valuation.modules;

import com.acuo.persist.core.ImportService;
import com.acuo.persist.core.Neo4jImportService;
import com.acuo.valuation.providers.acuo.Neo4jSwapService;
import com.acuo.valuation.providers.acuo.TradeUploadServiceImpl;
import com.acuo.valuation.providers.clarus.services.ClarusMarginCalcService;
import com.acuo.valuation.providers.markit.services.*;
import com.acuo.valuation.services.*;
import com.google.inject.AbstractModule;

public class ServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Sender.class).to(PortfolioValuationsSender.class);
        bind(Retriever.class).to(PortfolioValuationsRetriever.class);
        bind(PricingService.class).to(MarkitPricingService.class);
        bind(MarginCalcService.class).to(ClarusMarginCalcService.class);
        bind(SwapService.class).to(Neo4jSwapService.class);
        bind(TradeUploadService.class).to(TradeUploadServiceImpl.class);
    }

}
