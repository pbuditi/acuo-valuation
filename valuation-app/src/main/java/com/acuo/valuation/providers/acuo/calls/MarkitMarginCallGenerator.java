package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.ClientSignsRelation;
import com.acuo.persist.entity.CounterpartSignsRelation;
import com.acuo.persist.entity.LegalEntity;
import com.acuo.persist.entity.MarginStatement;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValueRelation;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.StatementDirection;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.acuo.valuation.utils.LocalDateUtils;
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

    protected Optional<VariationMargin> convert(TradeValuation valuation, LocalDate date, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Optional<List<TradeValueRelation>> current = tradeValueRelation(valuation, date);
        Optional<List<TradeValueRelation>> previous = tradeValueRelation(valuation, LocalDateUtils.minus(date, 1));
        Optional<Double> amount = computeAmount(current, previous);
        if (amount.isPresent()) {
            VariationMargin margin = process(amount.get(), Currency.USD, agreement, date, statementStatus, rates);
            marginCallService.save(margin);
            return Optional.of(margin);
        }
        return Optional.empty();
    }

    protected VariationMargin process(Double value, Currency currency, Agreement agreement, LocalDate date, StatementStatus statementStatus, Map<Currency, Double> rates) {
        VariationMargin variationMargin = new VariationMargin(value, date, currency, statementStatus, agreement, rates);
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

    private Optional<List<TradeValueRelation>> tradeValueRelation(TradeValuation valuation, LocalDate date) {
        Set<TradeValueRelation> values = valuation.getValues();
        if (values != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            final List<TradeValueRelation> result = valuation.getValues()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(valueRelation -> formatter.format(valueRelation.getDateTime()).equals(formatter.format(date)))
                    .collect(toList());
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Optional<Double> computeAmount(Optional<List<TradeValueRelation>> current, Optional<List<TradeValueRelation>> previous) {
        if (current.isPresent() && previous.isPresent()) {
            Double a = current.get().stream().mapToDouble(value ->  value.getValue().getPv()).sum();
            Double b = previous.get().stream().mapToDouble(value ->  value.getValue().getPv()).sum();
            return Optional.of(a - b);
        }
        return Optional.empty();
    }
}