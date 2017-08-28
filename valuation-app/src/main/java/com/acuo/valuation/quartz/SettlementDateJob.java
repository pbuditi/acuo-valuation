package com.acuo.valuation.quartz;

import com.acuo.valuation.providers.acuo.assets.SettlementDateProcessor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;

public class SettlementDateJob implements Job {

    private final SettlementDateProcessor settlementDateProcessor;

    @Inject
    public SettlementDateJob(SettlementDateProcessor settlementDateProcessor) {
        this.settlementDateProcessor = settlementDateProcessor;
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        settlementDateProcessor.process();
    }
}
