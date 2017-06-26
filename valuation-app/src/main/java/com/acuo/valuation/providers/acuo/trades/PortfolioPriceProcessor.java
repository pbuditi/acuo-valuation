package com.acuo.valuation.providers.acuo.trades;

import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.providers.acuo.results.MarkitResultPersister;
import com.acuo.valuation.services.PricingService;

import javax.inject.Inject;
import java.util.List;

public class PortfolioPriceProcessor {

    private final PricingService pricingService;
    private final MarkitResultPersister markitResultPersister;

    @Inject
    public PortfolioPriceProcessor(PricingService pricingService, MarkitResultPersister markitResultPersister)
    {
        this.pricingService = pricingService;
        this.markitResultPersister = markitResultPersister;
    }

    public void process(List<PortfolioId> portfolioIds)
    {
        MarkitResults markitResults = pricingService.pricePortfolios(portfolioIds);
        if(markitResults != null)
            markitResultPersister.persist(markitResults);
    }
}
