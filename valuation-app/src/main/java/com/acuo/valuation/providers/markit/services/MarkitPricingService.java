package com.acuo.valuation.providers.markit.services;

import com.acuo.common.util.LocalDateUtils;
import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.entity.TradeValuation;
import com.acuo.persist.entity.TradeValue;
import com.acuo.persist.ids.ClientId;
import com.acuo.persist.ids.PortfolioId;
import com.acuo.persist.ids.TradeId;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.ValuationService;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.MarkitResults;
import com.acuo.valuation.services.PricingService;
import com.acuo.valuation.builders.TradeBuilder;
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
    private final ValuationService valuationService;

    @Inject
    MarkitPricingService(Sender sender, Retriever retriever, TradeService<Trade> tradeService,ValuationService valuationService) {
        this.sender = sender;
        this.retriever = retriever;
        this.tradeService = tradeService;
        this.valuationService = valuationService;
    }

    @Override
    public MarkitResults priceTradeIds(List<String> swapIds) {
        List<com.acuo.common.model.trade.Trade> trades = swapIds.stream()
                .map(id -> tradeService.find(TradeId.fromString(id)))
                .filter(Objects::nonNull)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .map(TradeBuilder::buildTrade)
                .collect(toList());
        return priceSwapTrades(trades);
    }

    @Override
    public MarkitResults priceTradesOf(ClientId clientId) {
        Iterable<Trade> trades = tradeService.findBilateralTradesByClientId(clientId);
        List<com.acuo.common.model.trade.Trade> swapTrades = StreamSupport.stream(trades.spliterator(), false)
                .filter(trade -> trade instanceof IRS)
                .map(trade -> TradeBuilder.buildTrade(trade))
                .collect(toList());
        return priceSwapTrades(swapTrades);
    }

    @Override
    public MarkitResults priceTradesUnder(PortfolioId portfolioId) {
        Iterable<Trade> all = tradeService.findByPortfolioId(portfolioId);
        List<com.acuo.common.model.trade.Trade> filtered = StreamSupport.stream(all.spliterator(), false)
                .map(trade -> tradeService.find(trade.getTradeId(), 2))
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .filter(irs -> "Bilateral".equalsIgnoreCase(irs.getTradeType()))
                .map(TradeBuilder::buildTrade)
                .collect(toList());
        return priceSwapTrades(filtered);
    }

    @Override
    public MarkitResults pricePortfolios(List<PortfolioId> portfolioIds)
    {
        List<Trade> tradeList = portfolioIds.stream().map(portfolioId -> tradeService.findByPortfolioId(portfolioId)).flatMap(trades -> StreamSupport.stream(trades.spliterator(), false)).collect(Collectors.toList());
        if(tradeList == null || tradeList.size() == 0)
            return null;
        List<com.acuo.common.model.trade.Trade> filtered = tradeList.stream()
                .map(trade -> tradeService.find(trade.getTradeId(), 2))
                .filter(trade -> trade instanceof IRS)
                .map(trade -> (IRS) trade)
                .filter(irs -> "Bilateral".equalsIgnoreCase(irs.getTradeType()))
                .filter(irs -> !isTradeValuated(irs))
                .map(TradeBuilder::buildTrade)
                .collect(toList());
        return priceSwapTrades(filtered);
    }

    boolean isTradeValuated(IRS irs)
    {
        TradeValuation tradeValuation = valuationService.getTradeValuationFor(irs.getTradeId());
        if(tradeValuation != null && tradeValuation.getValues() != null)
        {
            for(TradeValue tradeValue : tradeValuation.getValues())
            {
                if(tradeValue.getValuationDate().equals(LocalDate.now()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public MarkitResults priceTradesOfType(String type) {
        Iterable<IRS> trades = tradeService.findAllIRS();
        List<com.acuo.common.model.trade.Trade> tradeIds = StreamSupport.stream(trades.spliterator(), false)
                .map(TradeBuilder::buildTrade)
                .collect(toList());
        return priceSwapTrades(tradeIds);
    }

    @Override
    public MarkitResults priceSwapTrades(List<com.acuo.common.model.trade.Trade> trades) {

        LocalDate valuationDate = LocalDateUtils.minus(LocalDate.now(), 1);

        Report report = sender.send(trades, valuationDate);
        Predicate<? super String> errorReport = getPredicate(report);

        List<String> tradeIds = trades
                .stream()
                .map(swap -> swap.getInfo().getTradeId())
                .filter(errorReport)
                .collect(Collectors.toList());

        return retriever.retrieve(valuationDate, tradeIds);
    }

    @Override
    public MarkitResults priceSwapTradesByBulk(List<com.acuo.common.model.trade.Trade> trades) {

        LocalDate valuationDate = LocalDateUtils.minus(LocalDate.now(), 1);

        Map<String, List<com.acuo.common.model.trade.Trade>> bulks = trades.stream()
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

    private CompletableFuture<List<String>> calculateAsyncWithCancellation(List<com.acuo.common.model.trade.Trade> trades, LocalDate valuationDate) {
        return CompletableFuture.supplyAsync(() -> {
            Report report = sender.send(trades, valuationDate);
            Predicate<? super String> errorReport = getPredicate(report);

            return trades
                    .stream()
                    .map(swap -> swap.getInfo().getTradeId())
                    .filter(errorReport)
                    .collect(Collectors.toList());
        }, Executors.newFixedThreadPool(30));
    }
}
