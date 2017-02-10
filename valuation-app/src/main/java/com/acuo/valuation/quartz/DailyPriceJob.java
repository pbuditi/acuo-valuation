package com.acuo.valuation.quartz;

import com.acuo.persist.entity.IRS;
import com.acuo.persist.entity.Trade;
import com.acuo.persist.services.MarginStatementService;
import com.acuo.persist.services.TradeService;
import com.acuo.persist.services.TradeServiceImpl;
import com.acuo.valuation.services.SwapService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class DailyPriceJob implements Job {

    private final SwapService swapService;

    @Inject
    public DailyPriceJob(SwapService swapService)
    {
        this.swapService = swapService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        swapService.valuationAllBilateralIRS();
    }
}
