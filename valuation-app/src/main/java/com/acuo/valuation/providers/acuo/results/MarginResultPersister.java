package com.acuo.valuation.providers.acuo.results;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.MarginValue;
import com.acuo.common.model.ids.PortfolioId;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.protocol.results.MarginValuation;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class MarginResultPersister implements ResultPersister<MarginResults> {

    private final ValuationService valuationService;
    private final ValueService valueService;

    @Inject
    public MarginResultPersister(ValuationService valuationService, ValueService valueService) {
        this.valuationService = valuationService;
        this.valueService = valueService;
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
                .filter(marginValuation -> marginValuation.getPortfolioId() != null)
                .map(marginValuation -> convert(valuationDate, currency, marginValuation))
                .collect(toList());
        if(values.isEmpty())
            return Collections.emptySet();
        valueService.save(values);
        return values.stream()
                .map(value -> value.getValuation().getPortfolio().getPortfolioId())
                .collect(toSet());
    }

    private MarginValue convert(LocalDate valuationDate, String currency, MarginValuation marginValuation) {
        String portfolioId = marginValuation.getPortfolioId();
        final Types.CallType callType = marginValuation.getMarginType();
        com.acuo.persist.entity.MarginValuation valuation = valuationService.getOrCreateMarginValuationFor(PortfolioId.fromString(portfolioId), callType);

        Set<MarginValue> values = valuation.getValues();
        if(values != null) {
            Set<MarginValue> toRemove = values.stream()
                    .filter(relation -> valuationDate.equals(relation.getValuationDate()))
                    .collect(toSet());
            values.removeAll(toRemove);
        }

        // reverse the margin for IM
        final Double value = (callType.equals(Types.CallType.Initial)) ? -1*marginValuation.getAccount() : marginValuation.getAccount();

        MarginValue newValue = createValue(valuationDate, currency, value, "Clarus");
        newValue.setValuation(valuation);

        return newValue;
    }

    private MarginValue createValue(LocalDate valuationDate, String currency, Double amount, String source) {
        MarginValue newValue = new MarginValue();
        newValue.setAmount(amount);
        newValue.setSource(source);
        newValue.setCurrency(Currency.of(currency));
        newValue.setValuationDate(valuationDate);
        newValue.setTimestamp(Instant.now());
        return newValue;
    }
}