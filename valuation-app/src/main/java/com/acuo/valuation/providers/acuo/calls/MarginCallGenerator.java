package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginStatement;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.StatementDirection;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.services.MarginCallGenService;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class MarginCallGenerator<V extends Valuation> implements MarginCallGenService {

    final ValuationService valuationService;
    private final MarginStatementService marginStatementService;
    private final AgreementService agreementService;

    private final CurrencyService currencyService;

    MarginCallGenerator(ValuationService valuationService,
                        MarginStatementService marginStatementService,
                        AgreementService agreementService,
                        CurrencyService currencyService) {
        this.valuationService = valuationService;
        this.marginStatementService = marginStatementService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
    }

    public List<VariationMargin> createCalls(Set<PortfolioId> portfolioSet, LocalDate valuationDate, LocalDate callDate) {
        log.info("generating margin calls for {}", portfolioSet);
        List<VariationMargin> marginCalls = portfolioSet.stream()
                .map(valuationsFunction())
                .map(valuation -> createcalls(valuation, valuationDate, callDate, statementStatusSupplier().get()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        log.info("{} margin calls generated", marginCalls.size());
        return marginCalls;
    }

    protected abstract Function<PortfolioId, V> valuationsFunction();

    protected abstract Supplier<StatementStatus> statementStatusSupplier();

    private Optional<VariationMargin> createcalls(V valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus) {
        final Agreement agreement = agreementService.agreementFor(valuation.getPortfolio().getPortfolioId());
        final Map<Currency, Double> rates = currencyService.getAllFX();
        return convert(valuation, valuationDate, callDate, statementStatus, agreement, rates);
    }

    protected abstract Optional<VariationMargin> convert(V valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates);

    protected VariationMargin process(Double amount, Currency currency, Agreement agreement, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Map<Currency, Double> rates) {
        VariationMargin variationMargin = new VariationMargin(amount, valuationDate, callDate, currency, statementStatus, agreement, rates);
        StatementDirection direction = variationMargin.getDirection();
        MarginStatement marginStatement = marginStatementService.getOrCreateMarginStatement(agreement, callDate, direction);
        variationMargin.setMarginStatement(marginStatement);
        return variationMargin;
    }
}