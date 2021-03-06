package com.acuo.valuation.providers.acuo.results;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.ErrorValue;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.entity.Value;
import com.acuo.common.model.ids.PortfolioId;
import com.acuo.common.model.ids.TradeId;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.collect.result.ValueWithFailures;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;

@Slf4j
public class MarkitResultPersister implements ResultPersister<MarkitResults> {

    private final ValuationService valuationService;
    private final ValueService valueService;

    @Inject
    public MarkitResultPersister(ValuationService valuationService, ValueService valueService) {
        this.valuationService = valuationService;
        this.valueService = valueService;
    }

    public Set<PortfolioId> persist(MarkitResults markitResults) {

        if (markitResults == null) {
            log.warn("markitResults is null");
            return Collections.emptySet();
        }

        log.info("persisting {} markit result of {}", markitResults.getResults().size(), markitResults.getValuationDate());

        LocalDate date = markitResults.getValuationDate();
        Currency currency = markitResults.getCurrency();

        List<Result<MarkitValuation>> results = markitResults.getResults();
        List<Value> values = results.stream()
                .flatMap(Result::stream)
                .map(value -> convert(date, currency, value))
                .flatMap(Collection::stream)
                .collect(toList());
        valueService.save(values, 1);
        List<MarginValue> marginValues = generate(values);
        valueService.save(marginValues, 1);
        Set<PortfolioId> portfolioIds = marginValues.stream()
                .map(value -> value.getValuation().getPortfolio().getPortfolioId())
                .collect(toSet());
        return portfolioIds;
    }

    private List<Value> convert(LocalDate date, Currency currency, MarkitValuation value) {
        List<Value> values = new ArrayList<>();
        String tradeId = value.getTradeId();
        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));

        ValueWithFailures<Double> result = value.getValue();
        TradeValue newValue = createValue(date, currency, result.getValue(), "Markit");
        newValue.setValuation(valuation);

        values.add(newValue);

        List<ErrorValue> errorValues = result.getFailures().stream()
                .map(failureItem -> {
                    ErrorValue error = new ErrorValue();
                    error.setReason(failureItem.getReason().name());
                    error.setMessage(failureItem.getMessage());
                    error.setValuation(valuation);
                    return error;
                })
                .collect(toList());

        values.addAll(errorValues);

        return values;
    }

    private List<MarginValue> generate(List<Value> values) {
        Map<LocalDate, Map<PortfolioId, Map<Currency, List<Double>>>> map = values.stream()
                .filter(value -> value instanceof TradeValue)
                .map(value -> (TradeValue)value)
                .collect(
                        groupingBy(this::valuationDate,
                                groupingBy(this::portfolioId,
                                        groupingBy(this::currency,
                                                mapping(TradeValue::getPv, toList())
                                        )
                                )
                        )
                );
        return convert(map);
    }

    private PortfolioId portfolioId(TradeValue value) {
        return value.getValuation().getTrade().getPortfolio().getPortfolioId();
    }

    private Currency currency(TradeValue value) {
        return value.getCurrency();
    }

    private LocalDate valuationDate(TradeValue value) {
        return value.getValuationDate();
    }


    private TradeValue createValue(LocalDate valuationDate, Currency currency, Double pv, String source) {
        TradeValue newValue = new TradeValue();
        newValue.setSource(source);
        newValue.setCurrency(currency);
        newValue.setPv(pv);
        newValue.setValuationDate(valuationDate);
        newValue.setTimestamp(Instant.now());
        return newValue;
    }

    private List<MarginValue> convert(Map<LocalDate, Map<PortfolioId, Map<Currency, List<Double>>>> dates) {
        List<MarginValue> results = new ArrayList<>();
        for (final LocalDate valuationDate : dates.keySet()) {
            Map<PortfolioId, Map<Currency, List<Double>>> portfolios = dates.get(valuationDate);
            for (final PortfolioId portfolioId : portfolios.keySet()) {
                Map<Currency, List<Double>> currencies = portfolios.get(portfolioId);
                for (final Currency currency : currencies.keySet()) {
                    List<Double> values = currencies.get(currency);
                    MarginValue marginValue = convert(valuationDate, portfolioId, currency, values.stream().mapToDouble(value -> value).sum());
                    results.add(marginValue);
                }
            }
        }
        return results;
    }

    private MarginValue convert(LocalDate valuationDate, PortfolioId portfolioId, Currency currency, Double value) {

        com.acuo.persist.entity.MarginValuation valuation = valuationService.getOrCreateMarginValuationFor(portfolioId, Types.CallType.Variation);

        Set<MarginValue> values = valuation.getValues();
        if(values != null) {
            Set<MarginValue> toRemove = values.stream()
                    .filter(relation -> valuationDate.equals(relation.getValuationDate()))
                    .collect(toSet());
            values.removeAll(toRemove);
        }

        MarginValue newValue = createMarginValue(valuationDate, currency, value, "Markit");
        newValue.setValuation(valuation);

        return newValue;
    }

    private MarginValue createMarginValue(LocalDate valuationDate, Currency currency, Double amount, String source) {
        MarginValue newValue = new MarginValue();
        newValue.setAmount(amount);
        newValue.setSource(source);
        newValue.setCurrency(currency);
        newValue.setValuationDate(valuationDate);
        newValue.setTimestamp(Instant.now());
        return newValue;
    }
}