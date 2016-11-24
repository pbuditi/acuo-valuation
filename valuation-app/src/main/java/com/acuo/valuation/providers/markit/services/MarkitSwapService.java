package com.acuo.valuation.providers.markit.services;

import com.acuo.common.model.trade.SwapTrade;
import com.acuo.entity.Client;
import com.acuo.persistence.Neo4jPersistService;
import com.acuo.valuation.protocol.reports.Report;
import com.acuo.valuation.protocol.results.SwapResults;
import com.acuo.valuation.services.SwapService;
import com.google.inject.Provider;
import org.neo4j.ogm.session.Session;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MarkitSwapService implements SwapService{

    private final Sender sender;
    private final Retriever retriever;

    @Inject
    protected Provider<Session> sessionProvider;

    @Inject
    public MarkitSwapService(Sender sender, Retriever retriever) {
        this.sender = sender;
        this.retriever = retriever;
    }


    @Override
    public SwapResults getPv(int swapId)
    {
        SwapTrade swapTrade = new SwapTrade();
        System.out.println(sessionProvider.get().loadAll(Client.class).toString());
//        //load swap object based on swapId
//
//        List<SwapTrade> swapTrades = new ArrayList<SwapTrade>();
//        swapTrades.add(swapTrade);
//
//        Report report = sender.send(swapTrades);
//
//        Predicate<? super String> errorReport = (Predicate<String>) tradeId -> {
//            List<Report.Item> items = report.itemsPerTradeId().get(tradeId);
//            if (items.stream().anyMatch(item -> "ERROR".equals(item.getType()))) {
//                return false;
//            }
//            return true;
//        };
//
//        List<String> tradeIds = swapTrades
//                .stream()
//                .map(swap -> swap.getInfo().getTradeId())
//                .filter(errorReport)
//                .collect(Collectors.toList());
//
//        //get the response,parse and return
//        //return retriever.retrieve(report.valuationDate(), tradeIds);
        return new SwapResults();
    }
}
