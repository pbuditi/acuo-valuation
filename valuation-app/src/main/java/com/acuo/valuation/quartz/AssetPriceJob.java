package com.acuo.valuation.quartz;

import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.AssetValue;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.providers.acuo.assets.AssetPricingProcessor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.util.Collection;

@Slf4j
public class AssetPriceJob implements Job {

    private final AssetPricingProcessor assetPricingProcessor;
    private final AssetService assetService;

    @Inject
    public AssetPriceJob(AssetPricingProcessor assetPricingProcessor, AssetService assetService) {
        this.assetPricingProcessor = assetPricingProcessor;
        this.assetService = assetService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("starting asset pricing job");
        Iterable<Asset> assets = assetService.findAll(1);
        final Collection<AssetValue> results = assetPricingProcessor.process(assets);
        log.info("asset pricing job complete with {} responses", results.size());
    }
}