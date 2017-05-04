package com.acuo.valuation.providers.acuo.results;

import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.MarginValueRelation;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.entity.TradeValueRelation;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.ValuationService;
import com.acuo.persist.services.ValueService;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.protocol.results.MarkitValuation;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.collect.result.Result;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;

@Slf4j
public class MarkitResultPersister extends AbstractResultProcessor<MarkitResults> implements ResultPersister<MarkitResults> {

    private final ValuationService valuationService;
    private final ValueService valueService;

    @Inject
    public MarkitResultPersister(ValuationService valuationService, ValueService valueService) {
        this.valuationService = valuationService;
        this.valueService = valueService;
    }

    @Override
    public ProcessorItem process(ProcessorItem<MarkitResults> processorItem) {
        log.info("processing markit valuation items");
        MarkitResults results = processorItem.getResults();
        Set<PortfolioId> portfolioIds = persist(results);
        processorItem.setPortfolioIds(portfolioIds);
        if (next != null)
            return next.process(processorItem);
        else
            return processorItem;
    }

    public Set<PortfolioId> persist(MarkitResults markitResults) {

        if (markitResults == null) {
            log.warn("markitResults is null");
            return Collections.emptySet();
        }

        log.info("persisting {} markit result of {}", markitResults.getResults().size(), markitResults.getDate());

        LocalDate date = markitResults.getDate();
        Currency currency = markitResults.getCurrency();

        List<Result<MarkitValuation>> results = markitResults.getResults();
        List<TradeValue> values = results.stream()
                .flatMap(Result::stream)
                .map(value -> convert(date, currency, value))
                .collect(toList());
        valueService.save(values);
        List<MarginValue> marginValues = generate(values);
        valueService.save(marginValues);
        Set<PortfolioId> portfolioIds = marginValues.stream()
                .map(value -> value.getValuation().getValuation().getPortfolio().getPortfolioId())
                .collect(toSet());
        return portfolioIds;
    }

    private TradeValue convert(LocalDate date, Currency currency, MarkitValuation value) {
        String tradeId = value.getTradeId();
        TradeValuation valuation = valuationService.getOrCreateTradeValuationFor(TradeId.fromString(tradeId));

        /*Set<TradeValueRelation> values = valuation.getValues();
        if(values != null) {
            Set<TradeValueRelation> toRemove = values.stream()
                    .filter(relation -> date.equals(relation.getDateTime()))
                    .collect(toSet());
            values.removeAll(toRemove);
        }*/

        TradeValue newValue = createValue(currency, value.getPv(), "Markit");
        TradeValueRelation valueRelation = new TradeValueRelation();
        valueRelation.setValuation(valuation);
        valueRelation.setDateTime(date);
        valueRelation.setValue(newValue);
        newValue.setValuation(valueRelation);

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

    private PortfolioId portfolioId(TradeValue value) {
        return value.getValuation().getValuation().getTrade().getPortfolio().getPortfolioId();
    }

    private Currency currency(TradeValue value) {
        return value.getCurrency();
    }

    private LocalDate valuationDate(TradeValue value) {
        return value.getValuation().getDateTime();
    }


    private TradeValue createValue(Currency currency, Double pv, String source) {
        TradeValue newValue = new TradeValue();
        newValue.setSource(source);
        newValue.setCurrency(currency);
        newValue.setPv(pv);
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

        com.acuo.persist.entity.MarginValuation valuation = valuationService.getOrCreateMarginValuationFor(portfolioId);

        MarginValue newValue = createMarginValue(currency, value, "Markit");
        MarginValueRelation valueRelation = new MarginValueRelation();
        valueRelation.setValuation(valuation);
        valueRelation.setDateTime(valuationDate);
        valueRelation.setValue(newValue);
        newValue.setValuation(valueRelation);

        return newValue;
    }

    private MarginValue createMarginValue(Currency currency, Double amount, String source) {
        MarginValue newValue = new MarginValue();
        newValue.setAmount(amount);
        newValue.setSource(source);
        newValue.setCurrency(currency);
        return newValue;
    }
}