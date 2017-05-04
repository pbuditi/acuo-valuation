package com.acuo.valuation.providers.acuo.calls;

import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.Side;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.ValuationService;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class MarkitMarginCallSimulator extends MarkitMarginCallGenerator {

    private final MarginCallService marginCallService;

    @Inject
    public MarkitMarginCallSimulator(ValuationService valuationService,
                                     MarginStatementService marginStatementService,
                                     MarginCallService marginCallService,
                                     CurrencyService currencyService,
                                     AgreementService agreementService) {
        super(valuationService,
                marginStatementService,
                marginCallService,
                currencyService,
                agreementService);
        this.marginCallService = marginCallService;
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Unrecon;
    }

    protected Supplier<Side> sideSupplier() {return () -> Side.Cpty;}

    protected VariationMargin process(Side side, Double value, Currency currency, StatementStatus statementStatus, Agreement agreement, LocalDate valuationDate, LocalDate callDate, Map<Currency, Double> rates) {
        java.util.Random r = new java.util.Random();
        double noise = r.nextGaussian() * Math.sqrt(0.2);
        double a = (0.2*noise);
        double amount = value * (1 + a);
        VariationMargin margin = super.process(side, amount, currency, statementStatus, agreement, valuationDate, callDate, rates);
        marginCallService.matchToExpected(margin.getItemId());
        return margin;
    }
}