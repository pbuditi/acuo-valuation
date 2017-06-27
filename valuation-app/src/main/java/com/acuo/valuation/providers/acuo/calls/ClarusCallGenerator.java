package com.acuo.valuation.providers.acuo.calls;

import com.acuo.common.model.margin.Types;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.MarginCall;
import com.acuo.persist.entity.MarginValue;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.AgreementService;
import com.acuo.persist.services.CurrencyService;
import com.acuo.persist.services.MarginCallService;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.PortfolioService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.acuo.results.ProcessorItem;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
public class ClarusCallGenerator extends CallGenerator<MarginResults> {

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
    public ProcessorItem process(ProcessorItem<MarginResults> processorItem) {
        log.info("processing markit valuation items to generate expected calls");
        LocalDate valuationDate = processorItem.getResults().getValuationDate();
        LocalDate callDate = LocalDateUtils.add(valuationDate, 1);
        Types.CallType callType = processorItem.getResults().getMarginType();
        Set<PortfolioId> portfolioIds = processorItem.getPortfolioIds();
        List<String> marginCalls = createCalls(portfolioIds, valuationDate, callDate, callType);
        processorItem.setExpected(marginCalls);
        if (next != null)
            return next.process(processorItem);
        else
            return processorItem;
    }

    protected Predicate<MarginValue> pricingSourcePredicate() {
        return value -> "Clarus".equals(value.getSource());
    }
}