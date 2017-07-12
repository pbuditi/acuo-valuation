package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.model.trade.Trade;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.acuo.ClarusValuationProcessor;
import com.acuo.valuation.providers.clarus.services.ClarusMarginService;

import javax.inject.Inject;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel.LCH;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType.IM;

public class ClarusIMProcessorImpl extends ClarusPricingProcessor {

    private final ClarusMarginService clarusMarginService;

    @Inject
    public ClarusIMProcessorImpl(ClarusValuationProcessor resultProcessor,
                                 ClarusMarginService clarusMarginService) {
        super(resultProcessor);
        this.clarusMarginService = clarusMarginService;
    }

    @Override
    protected MarginResults send(List<Trade> trades) {
        return clarusMarginService.send(trades, LCH, IM);
    }
}
