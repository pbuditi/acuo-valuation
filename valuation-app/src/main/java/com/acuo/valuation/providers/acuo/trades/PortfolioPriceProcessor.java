package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.model.ids.PortfolioId;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.acuo.valuation.services.PricingService;

import javax.inject.Inject;
import java.util.List;

public class PortfolioPriceProcessor {

    private final PricingService pricingService;
    private final ResultPersister<MarkitResults> resultPersister;

    @Inject
    public PortfolioPriceProcessor(PricingService pricingService, ResultPersister<MarkitResults> resultPersister) {
        this.pricingService = pricingService;
        this.resultPersister = resultPersister;
    }

    public void process(List<PortfolioId> portfolioIds) {
        MarkitResults markitResults = pricingService.pricePortfolios(portfolioIds);
        if (markitResults != null)
            resultPersister.persist(markitResults);
    }
}