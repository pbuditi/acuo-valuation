package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.model.margin.Types;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public class ClarusCallGenerator extends CallGenerator {

    @Inject
    ClarusCallGenerator(ValuationService valuationService,
                        MarginStatementService marginStatementService,
                        MarginCallService marginCallService,
                        CurrencyService currencyService,
                        AgreementService agreementService,
                        PortfolioService portfolioService) {
        super(valuationService,
              marginStatementService,
              marginCallService,
              currencyService,
              agreementService,
              portfolioService);
    }

    @Override
    public CallProcessorItem process(CallProcessorItem callProcessorItem) {
        log.info("processing markit valuation items to generate expected calls");
        LocalDate valuationDate = callProcessorItem.getValuationDate();
        LocalDate callDate = LocalDateUtils.add(valuationDate, 1);
        Types.CallType callType = callProcessorItem.getCallType();
        Set<PortfolioId> portfolioIds = callProcessorItem.getPortfolioIds();
        List<String> marginCalls = createCalls(portfolioIds, valuationDate, callDate, callType);
        callProcessorItem.setExpected(marginCalls);
        if (next != null)
            return next.process(callProcessorItem);
        else
            return callProcessorItem;
    }

    protected Predicate<MarginValue> pricingSourcePredicate() {
        return value -> "Clarus".equals(value.getSource());
    }
}