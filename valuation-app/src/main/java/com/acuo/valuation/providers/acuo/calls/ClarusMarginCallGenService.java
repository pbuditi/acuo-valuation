package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginValuation;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.entity.MarginValueRelation;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.ValuationService;
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
public class ClarusMarginCallGenService extends MarginCallGenerator<MarginValuation> {

    private final MarginCallService marginCallService;

    @Inject
    public ClarusMarginCallGenService(ValuationService valuationService,
                                      MarginStatementService marginStatementService,
                                      AgreementService agreementService,
                                      CurrencyService currencyService,
                                      MarginCallService marginCallService) {
        super(valuationService,
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

    protected Optional<VariationMargin> convert(MarginValuation valuation, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Agreement agreement, Map<Currency, Double> rates) {
        Optional<List<MarginValueRelation>> currents = marginValueRelation(valuation, valuationDate);
        Optional<Double> amount = currents.map(this::sum);
        return amount.map(aDouble -> {
            VariationMargin margin = process(aDouble, Currency.USD, agreement, valuationDate, callDate, statementStatus, rates);
            return marginCallService.save(margin);
        });
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
                .filter(value -> "Markit".equals(value.getSource()))
                .mapToDouble(MarginValue::getAmount)
                .sum();
    }
}