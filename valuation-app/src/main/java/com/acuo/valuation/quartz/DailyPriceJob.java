package com.acuo.valuation.quartz;

import com.acuo.valuation.services.PricingService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;

@Slf4j
public class DailyPriceJob implements Job {

    private final PricingService pricingService;

    @Inject
    public DailyPriceJob(PricingService pricingService)
    {
        this.pricingService = pricingService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        pricingService.priceTradesOfType("Bilateral");
    }
}
