package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.*;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;

import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
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

        Portfolio portfolio = portfolioService.find(PortfolioId.fromString(portfolioId));

        if (portfolio == null) return Collections.emptySet();

        //parse the result
        String currency = results.getCurrency();
        Iterator<Result<MarginValuation>> resultIterator = results.getResults().iterator();
        MarginValue newValue = new MarginValue();
        MarginValueRelation valueRelation = new MarginValueRelation();
        while (resultIterator.hasNext()) {
            Result<MarginValuation> result = resultIterator.next();
            MarginValuation marginValuation = result.getValue();
            if (marginValuation.getName().equals(currency)) {
                newValue.setAmount(marginValuation.getMargin());
                newValue.setSource("Clarus");
                newValue.setCurrency(Currency.of(currency));
                //newValue.setDate(results.getValuationDate());
                valueRelation.setValue(newValue);
                valueRelation.setDateTime(results.getValuationDate());
            }
        }

        com.acuo.persist.entity.MarginValuation valuation = valuationService.getOrCreateMarginValuationFor(PortfolioId.fromString(portfolioId));

        if (valuation == null) {
            valuation = new com.acuo.persist.entity.MarginValuation();
            valuation.setPortfolio(portfolio);
            valuationService.createOrUpdate(valuation);
        }

        if (valuation.getValues() == null)
            valuation.setValues(new HashSet<>());


        Set<MarginValueRelation> values = valuation.getValues();
        for (MarginValueRelation existedValue : values) {
            MarginValue tradeValue = existedValue.getValue();
            if (existedValue.getDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(results.getValuationDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                    && tradeValue.getCurrency().equals(currency) && tradeValue.getSource().equals("Clarus")) {
                valueService.delete(tradeValue.getId());
                break;
            }
        }
        valueRelation.setValuation(valuation);

        newValue.setValuation(valueRelation);
        valueService.createOrUpdate(newValue);

        return ImmutableSet.of(PortfolioId.fromString(portfolioId));
    }
}