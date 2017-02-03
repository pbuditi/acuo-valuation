package com.acuo.valuation.quartz;

import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.util.Iterator;

@Slf4j
public class DailyPriceJob implements Job {

//    final TradeService tradeService;
//
//    @Inject
//    public DailyPriceJob(TradeService tradeService)
//    {
//        this.tradeService = tradeService;
//    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        Iterator<Trade> trades = tradeService.findAll().iterator();
//        while(trades.hasNext())
//        {
//            log.info(trades.next().toString());
//        }
    }
}
