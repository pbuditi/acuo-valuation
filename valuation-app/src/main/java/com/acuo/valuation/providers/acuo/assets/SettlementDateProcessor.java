package com.acuo.valuation.providers.acuo.assets;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetSettlementDate;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.providers.reuters.services.SettlementDateService;
import com.acuo.valuation.utils.AssetsBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class SettlementDateProcessor {

    private final AssetService assetService;
    private final SettlementDateService settlementDateService;

    @Inject
    public SettlementDateProcessor(AssetService assetService, SettlementDateService settlementDateService)
    {
        this.assetService = assetService;
        this.settlementDateService = settlementDateService;
    }

    public void process()
    {
        List<Assets> assets = StreamSupport.stream(assetService.findAll().spliterator(), false).filter(asset -> asset.getType().equalsIgnoreCase("bond"))
                .map(AssetsBuilder::buildAssets).collect(Collectors.toList());
        log.info("assets to send :" + assets.toString());
        List<AssetSettlementDate> assetSettlementDates = settlementDateService.send(assets);
        log.info("settlementDate received :" + assetSettlementDates);

    }
}
