package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.PricingResults;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Slf4j
public class MarkitPricingService implements PricingService {

    private final Sender sender;
    private final Retriever retriever;
    private final TradeService<Trade> tradeService;

    @Inject
    public MarkitPricingService(Sender sender, Retriever retriever, TradeService<Trade> tradeService) {
        this.sender = sender;
        this.retriever = retriever;
        this.tradeService = tradeService;
    }

    @Override
    public PricingResults priceTradeIds(List<String> swapIds) {
        List<SwapTrade> swapTrades = swapIds.stream()
                .map(id -> tradeService.findById(id))
                .filter(trade -> trade != null)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .map(trade -> SwapTradeBuilder.buildTrade(trade))
                .collect(toList());
        return priceSwapTrades(swapTrades);
    }

    @Override
    public PricingResults priceTradesOf(ClientId clientId) {
        Iterable<Trade> trades = tradeService.findBilateralTradesByClientId(clientId);
        List<SwapTrade> swapTrades = StreamSupport.stream(trades.spliterator(), false)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> SwapTradeBuilder.buildTrade((IRS) trade))
                .collect(toList());
        return priceSwapTrades(swapTrades);
    }

    @Override
    public PricingResults priceTradesUnder(PortfolioId portfolioId) {
        Iterable<Trade> all = tradeService.findByPortfolioId(portfolioId);
        List<SwapTrade> filtered = StreamSupport.stream(all.spliterator(), false)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .filter(irs -> "Bilateral".equalsIgnoreCase(irs.getTradeType()))
                .map(irs -> SwapTradeBuilder.buildTrade(irs))
                .collect(toList());
        return priceSwapTrades(filtered);
    }

    @Override
    public PricingResults priceTradesOfType(String type) {
        Iterable<Trade> trades = tradeService.findAllIRS();
        List<SwapTrade> tradeIds = StreamSupport.stream(trades.spliterator(), false)
                .map(trade -> (IRS) tradeService.find(trade.getId()))
                .map(irs -> SwapTradeBuilder.buildTrade(irs))
                .collect(toList());
        return priceSwapTrades(tradeIds);
    }

    @Override
    public PricingResults priceSwapTrades(List<SwapTrade> swaps) {

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
        return retriever.retrieve(report.valuationDate().minusDays(1), tradeIds);
    }
}
