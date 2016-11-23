package com.acuo.valuation.providers.markit.services;

import com.acuo.valuation.protocol.results.SwapResults;
import com.acuo.valuation.services.SwapService;
import com.google.inject.Provider;

import javax.inject.Inject;

public class MarkitSwapService implements SwapService{

    //private final Sender sender;
   // private final Retriever retriever;

//    @Inject
//    public MarkitSwapService(Sender sender, Retriever retriever) {
//        this.sender = sender;
//        this.retriever = retriever;
//    }

    //private final Provider<Session> sessionProvider;

    @Override
    public SwapResults getPv(int swapId)
    {
        System.out.println("cantor test");
        return new SwapResults();
    }
}
