package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.model.margin.Types;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.Agreement;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.enums.Side;
import com.acuo.persist.entity.enums.StatementStatus;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class CallSimulator extends CallGenerator {

    private final MarginCallService marginCallService;
    private final Simulator simulationHelper;

    @Inject
    public CallSimulator(ValuationService valuationService,
                         MarginStatementService marginStatementService,
                         AgreementService agreementService,
                         CurrencyService currencyService,
                         MarginCallService marginCallService,
                         PortfolioService portfolioService,
                         Simulator simulationHelper) {
        super(valuationService,
                marginStatementService,
                marginCallService,
                currencyService,
                agreementService,
                portfolioService);
        this.marginCallService = marginCallService;
        this.simulationHelper = simulationHelper;
    }

    @Override
    public CallProcessorItem process(CallProcessorItem item) {
        log.info("processing {}", item);
        LocalDate valuationDate = item.getValuationDate();
        final Types.CallType callType = item.getCallType();
        LocalDate callDate = LocalDateUtils.add(valuationDate, 1);
        Set<PortfolioId> portfolioIds = item.getPortfolioIds();
        List<String> marginCalls = createCalls(portfolioIds, valuationDate, callDate, callType);
        log.info("simulated {} received calls", marginCalls.size());
        item.setSimulated(marginCalls);
        if (next != null)
            return next.process(item);
        else
            return item;
    }

    protected Supplier<StatementStatus> statementStatusSupplier() {
        return () -> StatementStatus.Received;
    }

    protected Supplier<Side> sideSupplier() {
        return () -> Side.Cpty;
    }

    protected MarginCall process(Types.CallType callType,
                                 Side side,
                                 Double value,
                                 Currency currency,
                                 StatementStatus statementStatus,
                                 Agreement agreement,
                                 LocalDate valuationDate,
                                 LocalDate callDate,
                                 Map<Currency, Double> rates,
                                 Long tradeCount) {
        if (simulationHelper.getRandomBoolean()) {
            double amount = simulationHelper.getRandomAmount(value);
            MarginCall margin = super.process(callType, side, amount, currency, statementStatus, agreement, valuationDate, callDate, rates, tradeCount);
            marginCallService.matchToExpected(margin.getItemId());
            return margin;
        } else {
            return null;
        }
    }
}
