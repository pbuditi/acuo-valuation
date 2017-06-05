package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.utils.SwapTradeBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MarkitPricingService implements PricingService {

    private final Sender sender;
    private final Retriever retriever;
    private final TradeService<Trade> tradeService;

    @Inject
    MarkitPricingService(Sender sender, Retriever retriever, TradeService<Trade> tradeService) {
        this.sender = sender;
        this.retriever = retriever;
        this.tradeService = tradeService;
    }

    @Override
    public MarkitResults priceTradeIds(List<String> swapIds) {
        List<SwapTrade> swapTrades = swapIds.stream()
                .map(id -> tradeService.find(TradeId.fromString(id)))
                .filter(Objects::nonNull)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .map(SwapTradeBuilder::buildTrade)
                .collect(toList());
        return priceSwapTrades(swapTrades);
    }

    @Override
    public MarkitResults priceTradesOf(ClientId clientId) {
        Iterable<Trade> trades = tradeService.findBilateralTradesByClientId(clientId);
        List<SwapTrade> swapTrades = StreamSupport.stream(trades.spliterator(), false)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> SwapTradeBuilder.buildTrade((IRS) trade))
                .collect(toList());
        return priceSwapTrades(swapTrades);
    }

    @Override
    public MarkitResults priceTradesUnder(PortfolioId portfolioId) {
        Iterable<Trade> all = tradeService.findByPortfolioId(portfolioId);
        List<SwapTrade> filtered = StreamSupport.stream(all.spliterator(), false)
                .map(trade -> tradeService.find(trade.getTradeId(), 2))
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .filter(irs -> "Bilateral".equalsIgnoreCase(irs.getTradeType()))
                .map(SwapTradeBuilder::buildTrade)
                .collect(toList());
        return priceSwapTrades(filtered);
    }

    @Override
    public MarkitResults priceTradesOfType(String type) {
        Iterable<IRS> trades = tradeService.findAllIRS();
        List<SwapTrade> tradeIds = StreamSupport.stream(trades.spliterator(), false)
                .map(SwapTradeBuilder::buildTrade)
                .collect(toList());
        return priceSwapTrades(tradeIds);
    }

    @Override
    public MarkitResults priceSwapTrades(List<SwapTrade> swaps) {

        LocalDate valuationDate = LocalDateUtils.minus(LocalDate.now(), 1);

        Report report = sender.send(swaps, valuationDate);
        Predicate<? super String> errorReport = getPredicate(report);

        List<String> tradeIds = swaps
                .stream()
                .map(swap -> swap.getInfo().getTradeId())
                .filter(errorReport)
                .collect(Collectors.toList());

        return retriever.retrieve(valuationDate, tradeIds);
    }

    @Override
    public MarkitResults priceSwapTradesByBulk(List<SwapTrade> swaps) {

        LocalDate valuationDate = LocalDateUtils.minus(LocalDate.now(), 1);

        Map<String, List<SwapTrade>> bulks = swaps.stream()
                .collect(groupingBy(trade -> trade.getInfo().getPortfolio(), toList()));

        final List<CompletableFuture<List<String>>> futures = bulks.keySet().stream()
                .map(bulks::get)
                .map(swapTrades -> calculateAsyncWithCancellation(swapTrades, valuationDate))
                .collect(toList());

        List<String> tradeIds = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .collect(toList());

        return retriever.retrieve(valuationDate, tradeIds);
    }

    private Predicate<? super String> getPredicate(Report report) {
        return (Predicate<String>) tradeId -> {
            List<Report.Item> items = report.itemsPerTradeId().get(tradeId);
            return !(items == null || items.stream().anyMatch(item -> "ERROR".equals(item.getType())));
        };
    }

    private CompletableFuture<List<String>> calculateAsyncWithCancellation(List<SwapTrade> swaps, LocalDate valuationDate) {
        return CompletableFuture.supplyAsync(() -> {
            Report report = sender.send(swaps, valuationDate);
            Predicate<? super String> errorReport = getPredicate(report);

            return swaps
                    .stream()
                    .map(swap -> swap.getInfo().getTradeId())
                    .filter(errorReport)
                    .collect(Collectors.toList());
        }, Executors.newFixedThreadPool(30));
    }
}
