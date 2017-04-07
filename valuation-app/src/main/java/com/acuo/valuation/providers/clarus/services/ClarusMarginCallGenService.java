package com.acuo.valuation.providers.clarus.services;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.ClientSignsRelation;
import com.acuo.persist.entity.CounterpartSignsRelation;
import com.acuo.persist.entity.LegalEntity;
import com.acuo.persist.entity.MarginStatement;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.MarginValueRelation;
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
import com.acuo.valuation.providers.acuo.calls.MarginCallGenerator;
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
public class ClarusMarginCallGenService extends MarginCallGenerator<MarginValuation> {

    private final MarginCallService marginCallService;

    @Inject
    public ClarusMarginCallGenService(ValuationService valuationService,
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
    protected Function<PortfolioId, MarginValuation> valuationsFunction() {
        return valuationService::getMarginValuationFor;
    }

    @Override
    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Expected;
    }

    protected List<VariationMargin> convert(MarginValuation valuation, LocalDate date, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Set<MarginValueRelation> values = valuation.getValues();
        if (values != null) {
            List<VariationMargin> margins = values
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(valueRelation -> valueRelation.getDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")).equals(date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))))
                    .map(MarginValueRelation::getValue)
                    .filter(value -> value.getSource().equals("Clarus"))
                    .map(value -> process(value, agreement, date, statementStatus, rates))
                    .collect(toList());
            marginCallService.save(margins);
            return margins;
        }
        return Collections.emptyList();
    }

    private VariationMargin process(MarginValue value, Agreement agreement, LocalDate date, StatementStatus statementStatus, Map<Currency, Double> rates) {
        VariationMargin variationMargin = new VariationMargin(value, statementStatus, agreement, rates);
        StatementDirection direction = variationMargin.getDirection();
        MarginStatement marginStatement = marginStatementService.getOrCreateMarginStatement(agreement, date, direction);
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
