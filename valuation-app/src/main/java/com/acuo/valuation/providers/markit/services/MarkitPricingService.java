package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.providers.markit.product.swap.IrSwap;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.ErrorResult;
import com.acuo.valuation.protocol.results.Result;
import com.acuo.valuation.services.PricingService;

import javax.inject.Inject;
import java.util.List;

public class MarkitPricingService implements PricingService {

    private final Sender sender;
    private final Retriever retriever;

    @Inject
    public MarkitPricingService(Sender sender, Retriever retriever) {
        this.sender = sender;
        this.retriever = retriever;
    }

    @Override
    public Result price(IrSwap swap) {
        String tradeId = swap.tradeId();

        Report report = sender.send(swap);

        List<Report.Item> items = report.itemsPerTradeId().get(tradeId);
        if (items.stream().anyMatch(item -> "ERROR".equals(item.getType()))) {
            return new ErrorResult();
        }

        return retriever.retrieve(report.valuationDate(), tradeId);
    }

}
