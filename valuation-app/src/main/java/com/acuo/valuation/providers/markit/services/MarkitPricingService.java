package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.PricingService;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MarkitPricingService implements PricingService {

    private final Sender sender;
    private final Retriever retriever;

    @Inject
    public MarkitPricingService(Sender sender, Retriever retriever) {
        this.sender = sender;
        this.retriever = retriever;
    }

    @Override
    public PricingResults price(List<SwapTrade> swaps) {

        Report report = sender.send(swaps);

        Predicate<? super String> errorReport = (Predicate<String>) tradeId -> {
            List<Report.Item> items = report.itemsPerTradeId().get(tradeId);
            if (items.stream().anyMatch(item -> "ERROR".equals(item.getType()))) {
                return false;
            }
            return true;
        };

        List<String> tradeIds = swaps
                .stream()
                .map(swap -> swap.getInfo().getTradeId())
                .filter(errorReport)
                .collect(Collectors.toList());

        return retriever.retrieve(report.valuationDate(), tradeIds);
    }

}
