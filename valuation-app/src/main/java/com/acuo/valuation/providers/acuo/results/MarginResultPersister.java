package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.MarginValueRelation;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class MarginResultPersister  extends AbstractResultProcessor<MarginResults> implements ResultPersister<MarginResults> {

    private final ValuationService valuationService;
    private final ValueService valueService;

    @Inject
    public MarginResultPersister(ValuationService valuationService, ValueService valueService) {
        this.valuationService = valuationService;
        this.valueService = valueService;
    }

    @Override
    public ProcessorItem process(ProcessorItem<MarginResults> processorItem) {
        log.info("processing markit valuation items");
        MarginResults results = processorItem.getResults();
        Set<PortfolioId> portfolioIds = persist(results);
        processorItem.setPortfolioIds(portfolioIds);
        if (next != null)
            return next.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public Set<PortfolioId> persist(MarginResults results) {

        if (results == null) {
            log.warn("margin results is null");
            return Collections.emptySet();
        }

        log.info("persisting {} markit result of {}", results.getResults().size(), results.getValuationDate());

        final LocalDate valuationDate = results.getValuationDate();
        final String currency = results.getCurrency();
        List<MarginValue> values = results.getResults()
                .stream()
                .map(Result::getValue)
                .map(marginValuation -> convert(valuationDate, currency, marginValuation))
                .collect(toList());
        valueService.save(values);
        Set<PortfolioId> portfolioIds = values.stream()
                .map(value -> value.getValuation().getValuation().getPortfolio().getPortfolioId())
                .collect(toSet());

        return portfolioIds;
    }

    private MarginValue convert(LocalDate valuationDate, String currency, MarginValuation marginValuation) {
        String portfolioId = marginValuation.getPortfolioId();
        com.acuo.persist.entity.MarginValuation valuation = valuationService.getOrCreateMarginValuationFor(PortfolioId.fromString(portfolioId));

        /*Set<MarginValueRelation> values = valuation.getValues();
        if(values != null) {
            Set<MarginValueRelation> toRemove = values.stream()
                    .filter(relation -> valuationDate.equals(relation.getDateTime()))
                    .collect(toSet());
            values.removeAll(toRemove);
        }*/

        MarginValue newValue = createValue(currency, marginValuation.getMargin(), "Clarus");
        MarginValueRelation valueRelation = new MarginValueRelation();
        valueRelation.setValuation(valuation);
        valueRelation.setDateTime(valuationDate);
        valueRelation.setValue(newValue);
        newValue.setValuation(valueRelation);

        return newValue;
    }

    private MarginValue createValue(String currency, Double amount, String source) {
        MarginValue newValue = new MarginValue();
        newValue.setAmount(amount);
        newValue.setSource(source);
        newValue.setCurrency(Currency.of(currency));
        //newValue.setDate(results.getValuationDate());
        return newValue;
    }
}