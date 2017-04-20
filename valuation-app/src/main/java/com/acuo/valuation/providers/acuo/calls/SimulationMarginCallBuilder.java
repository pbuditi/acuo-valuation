package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.VariationMargin;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.providers.acuo.results.MarkitValuationProcessor;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class SimulationMarginCallBuilder extends MarkitMarginCallGenerator implements MarkitValuationProcessor.PricingResultProcessor {

    private MarkitValuationProcessor.PricingResultProcessor nextProcessor;

    @Inject
    public SimulationMarginCallBuilder(ValuationService valuationService,
                                       MarginStatementService marginStatementService,
                                       AgreementService agreementService,
                                       CurrencyService currencyService,
                                       MarginCallService marginCallService) {
        super(valuationService,
                marginStatementService,
                agreementService,
                currencyService,
                marginCallService);
    }

    @Override
    public MarkitValuationProcessor.ProcessorItem process(MarkitValuationProcessor.ProcessorItem processorItem) {
        log.info("processing markit valuation items to generate simulated Unrecon calls");
        LocalDate valuationDate = processorItem.getResults().getDate();
        LocalDate callDate = LocalDateUtils.add(valuationDate, 1);
        Set<PortfolioId> portfolioIds = processorItem.getPortfolioIds();
        List<VariationMargin> marginCalls = createCalls(portfolioIds, valuationDate, callDate);
        processorItem.setSimulated(marginCalls);
        if (nextProcessor!= null)
            return nextProcessor.process(processorItem);
        else
            return processorItem;
    }

    @Override
    public void setNextProcessor(MarkitValuationProcessor.PricingResultProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Unrecon;
    }

    protected VariationMargin process(Double value, Currency currency, Agreement agreement, LocalDate valuationDate, LocalDate callDate, StatementStatus statementStatus, Map<Currency, Double> rates) {
        java.util.Random r = new java.util.Random();
        double noise = r.nextGaussian() * Math.sqrt(0.2);
        double a = (0.2*noise);
        double amount = value * (1 + a);
        return super.process(amount, currency, agreement, valuationDate, callDate, statementStatus, rates);
    }
}