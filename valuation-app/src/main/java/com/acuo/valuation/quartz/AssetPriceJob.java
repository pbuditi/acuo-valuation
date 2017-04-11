package com.acuo.valuation.quartz;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.providers.reuters.services.AssetsPersistService;
import com.acuo.valuation.providers.reuters.services.ReutersService;
import com.acuo.valuation.utils.AssetsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AssetPriceJob implements Job {

    private final ReutersService reutersService;
    private final AssetsPersistService assetsPersistService;
    private final AssetService assetService;

    @Inject
    public AssetPriceJob(ReutersService reutersService, AssetsPersistService assetsPersistService, AssetService assetService) {
        this.reutersService = reutersService;
        this.assetsPersistService = assetsPersistService;
        this.assetService = assetService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("starting asset pricing job");
        List<Assets> assetsList = new ArrayList<>();
        Iterable<Asset> assetIterable = assetService.findAll();
        assetIterable.forEach(asset -> assetsList.add(AssetsBuilder.buildAssets(asset)));
        List<AssetValuation> response = reutersService.send(assetsList);
        response.stream().forEach(a -> assetsPersistService.persist(a));
        log.info("asset pricing job complete");
    }
}
