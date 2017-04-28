package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.entity.TradeValueRelation;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.Side;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.providers.acuo.results.MarkitResultProcessor;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Slf4j
public class MarkitMarginCallGenerator extends MarginCallGenerator<TradeValuation> implements MarkitResultProcessor {

    protected final MarginCallService marginCallService;
    private MarkitResultProcessor nextProcessor;

    @Inject
    MarkitMarginCallGenerator(ValuationService valuationService,
                              MarginStatementService marginStatementService,
                              AgreementService agreementService,
                              CurrencyService currencyService,
                              MarginCallService marginCallService) {
        super(valuationService,
                marginStatementService,
                marginCallService, agreementService,
                currencyService);
        this.marginCallService = marginCallService;
    }

    @Override
    public MarkitValuationProcessor.ProcessorItem process(MarkitValuationProcessor.ProcessorItem processorItem) {
        log.info("processing markit valuation items to generate expected calls");
        LocalDate valuationDate = processorItem.getResults().getDate();
        LocalDate callDate = LocalDateUtils.add(valuationDate, 1);
        Set<PortfolioId> portfolioIds = processorItem.getPortfolioIds();
        List<VariationMargin> marginCalls = createCalls(portfolioIds, valuationDate, callDate);
        processorItem.setExpected(marginCalls);
        if (nextProcessor!= null)
            return nextProcessor.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public void setNext(MarkitResultProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    protected Function<PortfolioId, TradeValuation> valuationsFunction() {
        return valuationService::getTradeValuationFor;
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Expected;
    }

    protected Supplier<Side> sideSupplier() {return () -> Side.Client;}

    protected Optional<VariationMargin> convert(Side side, TradeValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Optional<List<TradeValueRelation>> current = tradeValueRelation(valuation, valuationDate);
        Optional<Double> amount = current.map(this::sum);
        return amount.map(aDouble -> process(side, aDouble, Currency.USD, statementStatus, agreement, valuationDate, callDate, rates));
    }

    private Optional<List<TradeValueRelation>> tradeValueRelation(TradeValuation valuation, LocalDate valuationDate) {
        Set<TradeValueRelation> values = valuation.getValues();
        if (values != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            final List<TradeValueRelation> result = valuation.getValues()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(valueRelation -> formatter.format(valueRelation.getDateTime()).equals(formatter.format(valuationDate)))
                    .collect(toList());
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Double sum(List<TradeValueRelation> relations) {
        return relations.stream()
                .map(TradeValueRelation::getValue)
                .filter(value -> "Markit".equals(value.getSource()))
                .mapToDouble(TradeValue::getPv)
                .sum();
    }
}