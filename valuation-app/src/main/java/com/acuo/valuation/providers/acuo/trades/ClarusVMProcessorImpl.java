package com.acuo.valuation.providers.acuo.trades;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.results.MarginResults;
import com.acuo.valuation.providers.acuo.results.ClarusValuationProcessor;
import com.acuo.valuation.services.MarginCalcService;

import javax.inject.Inject;
import java.util.List;

import static com.acuo.valuation.providers.clarus.protocol.Clarus.DataModel.LCH;
import static com.acuo.valuation.providers.clarus.protocol.Clarus.MarginCallType.VM;

public class ClarusVMProcessorImpl extends ClarusPricingProcessor {

    private final MarginCalcService marginCalcService;

    @Inject
    public ClarusVMProcessorImpl(ClarusValuationProcessor resultProcessor,
                                 MarginCalcService marginCalcService) {
        super(resultProcessor);
        this.marginCalcService = marginCalcService;
    }

    @Override
    protected MarginResults send(List<SwapTrade> swapTrades) {
        return marginCalcService.send(swapTrades, LCH, VM);
    }
}
