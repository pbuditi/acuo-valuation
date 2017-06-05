package com.acuo.valuation.quartz;

import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.TradeService;
import com.acuo.valuation.providers.acuo.trades.TradePricingProcessor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;

@Slf4j
public class GenerateCallJob implements Job {

    private final TradeService<Trade> tradeService;
    private final TradePricingProcessor tradePricingProcessor;

    @Inject
    public GenerateCallJob(TradeService<Trade> tradeService,
                           TradePricingProcessor tradePricingProcessor){
        this.tradeService = tradeService;
        this.tradePricingProcessor = tradePricingProcessor;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Generating margin calls from all swaps");
        Iterable<Trade> trades = tradeService.findAll(2);
        tradePricingProcessor.process(trades);
        log.info("margin calls generation job complete");
    }
}