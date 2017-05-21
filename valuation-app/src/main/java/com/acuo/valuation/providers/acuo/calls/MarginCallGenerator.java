package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.model.margin.Types;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.InitialMargin;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.MarginStatement;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.Side;
import com.acuo.persist.entity.enums.StatementDirection;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.providers.acuo.results.AbstractResultProcessor;
import com.acuo.valuation.services.MarginCallGenService;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class MarginCallGenerator<R> extends AbstractResultProcessor<R> implements MarginCallGenService {

    private final ValuationService valuationService;
    private final MarginStatementService marginStatementService;
    private final MarginCallService marginCallService;
    private final AgreementService agreementService;
    private final CurrencyService currencyService;
    private final PortfolioService portfolioService;

    MarginCallGenerator(ValuationService valuationService,
                        MarginStatementService marginStatementService,
                        MarginCallService marginCallService,
                        CurrencyService currencyService,
                        AgreementService agreementService,
                        PortfolioService portfolioService) {
        this.valuationService = valuationService;
        this.marginStatementService = marginStatementService;
        this.marginCallService = marginCallService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
        this.portfolioService = portfolioService;
    }

    public List<MarginCall> createCalls(Set<PortfolioId> portfolioSet, LocalDate valuationDate, LocalDate callDate, Types.CallType callType) {
        log.info("generating margin calls for {}", portfolioSet);
        List<MarginCall> marginCalls = portfolioSet.stream()
                .map(id -> valuationService.getMarginValuationFor(id, callType))
                .map(valuation -> createcalls(sideSupplier().get(), valuation, valuationDate, callDate, statementStatusSupplier().get()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        log.info("{} margin calls generated", marginCalls.size());
        return marginCalls;
    }

    private Optional<MarginCall> createcalls(Side side, MarginValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus) {
        final Agreement agreement = agreementService.agreementFor(valuation.getPortfolio().getPortfolioId());
        final Map<Currency, Double> rates = currencyService.getAllFX();
        return convert(side, valuation, valuationDate, callDate, statementStatus, agreement, rates);
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Expected;
    }

    protected Supplier<Side> sideSupplier() {return () -> Side.Client;}

    protected abstract Predicate<MarginValue> pricingSourcePredicate();

    private Optional<MarginCall> convert(Side side, MarginValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Optional<List<MarginValue>> currents = marginValueRelation(valuation, valuationDate);
        Types.CallType callType = valuation.getCallType();
        Optional<Double> amount = currents.map(this::sum);
        Long tradeCount = portfolioService.tradeCount(valuation.getPortfolio().getPortfolioId());
        return amount.map(aDouble -> process(callType, side, aDouble, Currency.USD, statementStatus, agreement, valuationDate, callDate, rates, tradeCount));
    }

    private Optional<List<MarginValue>> marginValueRelation(MarginValuation valuation, LocalDate valuationDate) {
        Set<MarginValue> values = valuation.getValues();
        if (values != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            List<MarginValue> result = valuation.getValues()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(value -> formatter.format(value.getDateTime()).equals(formatter.format(valuationDate)))
                    .collect(toList());
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Double sum(List<MarginValue> values) {
        return values.stream()
                .filter(pricingSourcePredicate())
                .mapToDouble(MarginValue::getAmount)
                .sum();
    }

    protected MarginCall process(Types.CallType callType,
                                      Side side,
                                      Double amount,
                                      Currency currency,
                                      StatementStatus statementStatus,
                                      Agreement agreement,
                                      LocalDate valuationDate,
                                      LocalDate callDate,
                                      Map<Currency, Double> rates,
                                      Long tradeCount) {
        MarginCall margin = null;
        if (callType.equals(Types.CallType.Variation)) {
            margin = new VariationMargin(side, amount, valuationDate, callDate, currency, agreement, rates, tradeCount);
        } else {
            margin = new InitialMargin(side, amount, valuationDate, callDate, currency, agreement, rates, tradeCount);
        }
        StatementDirection direction = margin.getDirection();
        MarginStatement marginStatement = marginStatementService.getOrCreateMarginStatement(agreement, callDate, direction);
        margin.setMarginStatement(marginStatement);
        margin = marginCallService.save(margin);
        marginStatementService.setStatus(margin.getItemId(), statementStatus);
        return margin;
    }
}