package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.Valuation;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.services.MarginCallGenService;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
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

    protected final ValuationService valuationService;
    protected final MarginStatementService marginStatementService;
    final PortfolioService portfolioService;
    private final AgreementService agreementService;

    private final CurrencyService currencyService;
    private DecimalFormat df = new DecimalFormat("#.##");

    public MarginCallGenerator(ValuationService valuationService,
                        PortfolioService portfolioService,
                        MarginStatementService marginStatementService,
                        AgreementService agreementService,
                        CurrencyService currencyService) {
        this.valuationService = valuationService;
        this.portfolioService = portfolioService;
        this.marginStatementService = marginStatementService;
        this.agreementService = agreementService;
        this.currencyService = currencyService;
    }

    public List<VariationMargin> createCalls(Set<PortfolioId> portfolioSet, LocalDate date) {
        log.info("generating margin calls for {}", portfolioSet);
        List<VariationMargin> marginCalls = portfolioSet.stream()
                .map(valuationsFunction())
                .map(valuation -> createcalls(valuation, date, statementStatusSupplier().get()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
        log.info("{} margin calls generated", marginCalls.size());
        return marginCalls;
    }

    protected abstract Function<PortfolioId, V> valuationsFunction();

    protected abstract Supplier<StatementStatus> statementStatusSupplier();

    private Optional<VariationMargin> createcalls(V valuation, LocalDate date, StatementStatus statementStatus) {
        final Agreement agreement = agreementService.agreementFor(valuation.getPortfolio().getPortfolioId());
        final Map<Currency, Double> rates = currencyService.getAllFX();
        return convert(valuation, date, statementStatus, agreement, rates);
    }

    protected abstract Optional<VariationMargin> convert(V valuation, LocalDate date, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates);


}