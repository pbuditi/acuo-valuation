package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.model.margin.Types;
import com.acuo.common.model.results.TradeValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.acuo.valuation.protocol.results.PortfolioResults;
import com.acuo.valuation.providers.acuo.results.ResultPersister;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.*;

@Slf4j
public class PortfolioValuationPersister implements ResultPersister<PortfolioResults> {

    private final ValuationService valuationService;
    private final ValueService valueService;

    @Inject
    public PortfolioValuationPersister(ValuationService valuationService, ValueService valueService) {
        this.valuationService = valuationService;
        this.valueService = valueService;
    }

    public Set<PortfolioId> persist(PortfolioResults results)
    {
        if (results == null || results.getResults().size() == 0) {
            log.warn("PortfolioResults is null");
            return Collections.emptySet();
        }

        log.info("persisting {} markit result of {}", results.getResults().size(), results.getValuationDate());

        LocalDate date = results.getValuationDate();
        Currency currency = results.getCurrency();

        LocalDate dayRange = LocalDate.now().minusDays(2);

        List<Result<TradeValuation>> result = results.getResults();
        List<TradeValue> values = result.stream()
                .flatMap(Result::stream)
                .filter(tradeValuation -> tradeValuation.getValuationDate().isAfter(dayRange))
                .map(value -> convert(currency, value))
                .collect(toList());
        valueService.save(values, 1);
        List<MarginValue> marginValues = generate(values);
        valueService.save(marginValues, 1);
        Set<PortfolioId> portfolioIds = marginValues.stream()
                .map(value -> value.getValuation().getPortfolio().getPortfolioId())
                .collect(toSet());
        return portfolioIds;
    }

    private TradeValue convert(Currency currency, TradeValuation value) {
        String tradeId = value.getTradeId();
        com.acuo.persist.entity.TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));

        TradeValue newValue = createValue(value.getValuationDate(), currency, value.getMarketValue(), "Portfolio");
        newValue.setValuation(valuation);

        return newValue;
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

    private List<MarginValue> generate(List<TradeValue> values) {
        Map<LocalDate, Map<PortfolioId, Map<Currency, List<Double>>>> map = values.stream()
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

        MarginValue newValue = createMarginValue(valuationDate, currency, value, "Portfolio");
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

    private PortfolioId portfolioId(TradeValue value) {
        return value.getValuation().getTrade().getPortfolio().getPortfolioId();
    }

    private Currency currency(TradeValue value) {
        return value.getCurrency();
    }

    private LocalDate valuationDate(TradeValue value) {
        return value.getValuationDate();
    }

}
