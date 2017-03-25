package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.Portfolio;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MarginResultPersister implements ResultPersister<MarginResults> {

    private final ValuationService valuationService;
    private final PortfolioService portfolioService;
    private final ValueService valueService;

    @Inject
    public MarginResultPersister(ValuationService valuationService, PortfolioService portfolioService, ValueService valueService) {
        this.valuationService = valuationService;
        this.portfolioService = portfolioService;
        this.valueService = valueService;
    }

    @Override
    public Set<PortfolioId> persist(MarginResults results) {
        String portfolioId = results.getPortfolioId();

        Portfolio portfolio = portfolioService.findById(portfolioId);

        if (portfolio == null) return Collections.emptySet();

        //parse the result
        String currency = results.getCurrency();
        Iterator<Result<MarginValuation>> resultIterator = results.getResults().iterator();
        TradeValue newValue = new TradeValue();
        while (resultIterator.hasNext()) {
            Result<MarginValuation> result = resultIterator.next();
            MarginValuation marginValuation = result.getValue();
            if (marginValuation.getName().equals(currency)) {
                newValue.setPv(marginValuation.getMargin());
                newValue.setSource("Clarus");
                newValue.setCurrency(Currency.of(currency));
                newValue.setDate(results.getValuationDate());
            }
        }

        Valuation valuation = portfolio.getValuation();

        if(valuation == null)
        {
            com.acuo.persist.entity.MarginValuation marginValuation = new com.acuo.persist.entity.MarginValuation();
            marginValuation.setPortfolio(portfolio);
            valuationService.createOrUpdate(marginValuation);
            valuation = marginValuation;
        }

        if(valuation.getValues() == null)
            valuation.setValues(new HashSet<>());


        Set<com.acuo.persist.entity.Value> values = valuation.getValues();
        for(com.acuo.persist.entity.Value existedValue : values)
        {
            TradeValue tradeValue = (TradeValue)existedValue;
            if(tradeValue.getDate().equals(results.getValuationDate()) && tradeValue.getCurrency().equals(currency) && tradeValue.getSource().equals("Clarus"))
            {
                valueService.delete(tradeValue.getId());
                values.remove(tradeValue);
            }
        }

        newValue.setValuation(valuation);
        valueService.createOrUpdate(newValue);

        return ImmutableSet.of(PortfolioId.fromString(portfolioId));
    }
}