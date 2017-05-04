package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginStatement;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.MarginValueRelation;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.Side;
import com.acuo.persist.entity.enums.StatementDirection;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
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
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class MarginCallGenerator<R> extends AbstractResultProcessor<R> implements MarginCallGenService {

    private final ValuationService valuationService;
    private final MarginStatementService marginStatementService;
    private final MarginCallService marginCallService;
    private final AgreementService agreementService;
    private final CurrencyService currencyService;

    MarginCallGenerator(ValuationService valuationService,
                        MarginStatementService marginStatementService,
                        MarginCallService marginCallService,
                        CurrencyService currencyService,
                        AgreementService agreementService) {
        this.valuationService = valuationService;
        this.marginStatementService = marginStatementService;
        this.marginCallService = marginCallService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
    }

    public List<VariationMargin> createCalls(Set<PortfolioId> portfolioSet, LocalDate valuationDate, LocalDate callDate) {
        log.info("generating margin calls for {}", portfolioSet);
        List<VariationMargin> marginCalls = portfolioSet.stream()
                .map(valuationService::getMarginValuationFor)
                .map(valuation -> createcalls(sideSupplier().get(), valuation, valuationDate, callDate, statementStatusSupplier().get()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        log.info("{} margin calls generated", marginCalls.size());
        return marginCalls;
    }

    private Optional<VariationMargin> createcalls(Side side, MarginValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus) {
        final Agreement agreement = agreementService.agreementFor(valuation.getPortfolio().getPortfolioId());
        final Map<Currency, Double> rates = currencyService.getAllFX();
        return convert(side, valuation, valuationDate, callDate, statementStatus, agreement, rates);
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Expected;
    }

    protected Supplier<Side> sideSupplier() {return () -> Side.Client;}

    private Optional<VariationMargin> convert(Side side, MarginValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Optional<List<MarginValueRelation>> currents = marginValueRelation(valuation, valuationDate);
        Optional<Double> amount = currents.map(this::sum);
        return amount.map(aDouble -> process(side, aDouble, Currency.USD, statementStatus, agreement, valuationDate, callDate, rates));
    }

    private Optional<List<MarginValueRelation>> marginValueRelation(MarginValuation valuation, LocalDate valuationDate) {
        Set<MarginValueRelation> values = valuation.getValues();
        if (values != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            List<MarginValueRelation> result = valuation.getValues()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(valueRelation -> formatter.format(valueRelation.getDateTime()).equals(formatter.format(valuationDate)))
                    .collect(toList());
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Double sum(List<MarginValueRelation> relations) {
        return relations.stream()
                .map(MarginValueRelation::getValue)
                .filter(value -> "Clarus".equals(value.getSource()))
                .mapToDouble(MarginValue::getAmount)
                .sum();
    }

    protected VariationMargin process(Side side, Double amount, Currency currency, StatementStatus statementStatus, Agreement agreement, LocalDate valuationDate, LocalDate callDate, Map<Currency, Double> rates) {
        VariationMargin variationMargin = new VariationMargin(side, amount, valuationDate, callDate, currency, agreement, rates);
        StatementDirection direction = variationMargin.getDirection();
        MarginStatement marginStatement = marginStatementService.getOrCreateMarginStatement(agreement, callDate, direction);
        variationMargin.setMarginStatement(marginStatement);
        variationMargin = marginCallService.save(variationMargin);
        marginStatementService.setStatus(variationMargin.getItemId(), statementStatus);
        return variationMargin;
    }
}