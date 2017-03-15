package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.MarginCallGenService;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class MarkitValautionsProcessor {

    private final ResultPersister<PricingResults> persister;
    private final MarginCallGenService marginCallGenService;

    @Inject
    public MarkitValautionsProcessor(ResultPersister<PricingResults> persister,
                                     MarginCallGenService marginCallGenService) {
        this.persister = persister;
        this.marginCallGenService = marginCallGenService;
    }

    public List<MarginCall> process(PricingResults results) {
        LocalDate date = results.getDate();
        Set<PortfolioId> portfolioIdSet =  persister.persist(results);
        return marginCallGenService.marginCalls(portfolioIdSet, date);
    }

}
