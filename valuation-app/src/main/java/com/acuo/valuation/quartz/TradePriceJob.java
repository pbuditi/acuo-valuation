package com.acuo.valuation.quartz;

import com.acuo.valuation.services.PricingService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;

@Slf4j
public class TradePriceJob implements Job {

    private final PricingService pricingService;

    @Inject
    public TradePriceJob(PricingService pricingService)
    {
        this.pricingService = pricingService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("starting trade pricing job");
        pricingService.priceTradesOfType("Bilateral");
        log.info("trade pricing job complete");
    }
}
