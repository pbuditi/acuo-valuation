package com.acuo.valuation.providers.acuo;

import com.acuo.persist.entity.Portfolio;
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

        if(portfolio == null) return Collections.emptySet();

        //parse the result
        String currency = results.getCurrency();
        Iterator<Result<MarginValuation>> resultIterator = results.getResults().iterator();
        Valuation newValuation = new Valuation();
        newValuation.setDate(results.getValuationDate());
        com.acuo.persist.entity.Value newValue = new com.acuo.persist.entity.Value();
        while(resultIterator.hasNext())
        {
            Result<MarginValuation> result = resultIterator.next();
            MarginValuation marginValuation= result.getValue();
            if(marginValuation.getName().equals(currency))
            {
                newValue.setPv(marginValuation.getMargin());
                newValue.setSource("Clarus");
                newValue.setCurrency(Currency.of(currency));
            }
        }



        Set<Valuation> valuations = portfolio.getValuations();
        if(valuations == null)
        {
            valuations = new HashSet<Valuation>();


        }

        for(Valuation valuation : valuations)
        {
            valuation = valuationService.find(valuation.getId());
            if(valuation.getDate().equals(results.getValuationDate()))
            {
                Set<com.acuo.persist.entity.Value> values = valuation.getValues();
                if(values == null)
                {
                    values = new HashSet<com.acuo.persist.entity.Value>();
                }
                for(com.acuo.persist.entity.Value value : values)
                {
                    if(value.getCurrency().equals(currency) && value.getSource().equals("Clarus"))
                    {
                        //replace this value
                        value.setPv(newValue.getPv());
                        valueService.createOrUpdate(value);
                    }
                }

                values.add(newValue);
                valuation.setValues(values);
                valuationService.createOrUpdate(valuation);
            }
        }

        valuations.add(newValuation);
        Set<com.acuo.persist.entity.Value> values = new HashSet<com.acuo.persist.entity.Value>();
        values.add(newValue);
        newValuation.setValues(values);
        portfolio.setValuations(valuations);
        valuationService.createOrUpdate(newValuation);
        portfolioService.createOrUpdate(portfolio);

        return ImmutableSet.of(PortfolioId.fromString(portfolioId));
    }
}
