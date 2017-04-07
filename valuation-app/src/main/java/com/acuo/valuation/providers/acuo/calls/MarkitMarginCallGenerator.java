package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.*;
import com.acuo.persist.entity.enums.StatementDirection;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.*;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.acuo.common.util.ArithmeticUtils.addition;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MarkitMarginCallGenerator extends MarginCallGenerator<TradeValuation> implements MarkitValuationProcessor.PricingResultProcessor {

    private final MarginCallService marginCallService;
    private MarkitValuationProcessor.PricingResultProcessor nextProcessor;

    @Inject
    public MarkitMarginCallGenerator(ValuationService valuationService,
                                     PortfolioService portfolioService,
                                     MarginStatementService marginStatementService,
                                     AgreementService agreementService,
                                     CurrencyService currencyService,
                                     MarginCallService marginCallService) {
        super(valuationService,
                portfolioService,
                marginStatementService,
                agreementService,
                currencyService);
        this.marginCallService = marginCallService;
    }

    @Override
    public MarkitValuationProcessor.ProcessorItem process(MarkitValuationProcessor.ProcessorItem processorItem) {
        log.info("processing markit valuation items");
        LocalDate date = processorItem.getResults().getDate();
        Set<PortfolioId> portfolioIds = processorItem.getPortfolioIds();
        List<VariationMargin> marginCalls = createCalls(portfolioIds, date);
        processorItem.setExpected(marginCalls);
        if (nextProcessor!= null)
            return nextProcessor.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public void setNextProcessor(MarkitValuationProcessor.PricingResultProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    protected Function<PortfolioId, TradeValuation> valuationsFunction() {
        return valuationService::getTradeValuationFor;
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Expected;
    }

    protected List<VariationMargin> convert(TradeValuation valuation, LocalDate date, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Set<TradeValueRelation> values = valuation.getValues();
        if (values != null) {
            List<VariationMargin> margins = values
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(valueRelation -> valueRelation.getDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))))
                    .map(TradeValueRelation::getValue)
                    .filter(value -> value.getSource().equals("Markit"))
                    .map(value -> process(value, agreement, date, statementStatus, rates))
                    .collect(toList());
            marginCallService.save(margins);
            return margins;
        }
        return Collections.emptyList();
    }

    protected VariationMargin process(TradeValue value, Agreement agreement, LocalDate date, StatementStatus statementStatus, Map<Currency, Double> rates) {
        VariationMargin variationMargin = new VariationMargin(value, statementStatus, agreement, rates);
        StatementDirection direction = variationMargin.getDirection();
        MarginStatement marginStatement = marginStatementService.getOrCreateMarginStatement(agreement, date, direction);
        variationMargin.setMarginStatement(marginStatement);
        ClientSignsRelation clientSignsRelation = agreement.getClientSignsRelation();
        CounterpartSignsRelation counterpartSignsRelation = agreement.getCounterpartSignsRelation();
        LegalEntity client = clientSignsRelation.getLegalEntity();
        LegalEntity counterpart = counterpartSignsRelation.getLegalEntity();
        if (direction.equals(StatementDirection.IN)) {
            marginStatement.setDirectedTo(counterpart);
            marginStatement.setSentFrom(client);
            marginStatement.setPendingCash(addition(clientSignsRelation.getInitialPending(), clientSignsRelation.getVariationPending()));
            marginStatement.setPendingNonCash(addition(clientSignsRelation.getInitialPendingNonCash(), clientSignsRelation.getVariationPendingNonCash()));
        } else {
            marginStatement.setDirectedTo(client);
            marginStatement.setSentFrom(counterpart);
            marginStatement.setPendingCash(addition(counterpartSignsRelation.getInitialPending(), counterpartSignsRelation.getVariationPending()));
            marginStatement.setPendingNonCash(addition(counterpartSignsRelation.getInitialPendingNonCash(), counterpartSignsRelation.getVariationPendingNonCash()));
        }
        marginStatementService.createOrUpdate(marginStatement);
        return variationMargin;
    }
}