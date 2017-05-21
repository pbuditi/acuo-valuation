package com.acuo.valuation.quartz;

import com.acuo.valuation.providers.datascope.service.intraday.DataScopeIntradayService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;

@Slf4j
public class FXRatesIntradayJob implements Job {

    private final DataScopeIntradayService intradayService;

    @Inject
    public FXRatesIntradayJob(DataScopeIntradayService intradayService) {
        this.intradayService = intradayService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("starting fx rate intraday job");
        intradayService.rates();
        log.info("fx rates service job complete");
    }
}
