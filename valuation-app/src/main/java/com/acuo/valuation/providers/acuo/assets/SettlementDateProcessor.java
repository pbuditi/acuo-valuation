package com.acuo.valuation.providers.acuo.assets;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.results.AssetSettlementDate;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.SettlementDate;
import com.acuo.persist.services.AssetService;
import com.acuo.valuation.providers.reuters.services.SettlementDateService;
import com.acuo.valuation.utils.AssetsBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class SettlementDateProcessor {

    private final AssetService assetService;
    private final SettlementDateService settlementDateService;
    private final com.acuo.persist.services.SettlementDateService service;


    @Inject
    public SettlementDateProcessor(AssetService assetService, SettlementDateService settlementDateService,
                                   com.acuo.persist.services.SettlementDateService service)
    {
        this.assetService = assetService;
        this.settlementDateService = settlementDateService;
        this.service = service;
    }

    public void process()
    {
        List<Assets> assets = StreamSupport.stream(assetService.findAll().spliterator(), false).filter(asset -> asset.getType().equalsIgnoreCase("bond"))
                .map(AssetsBuilder::buildAssets).collect(Collectors.toList());
        log.info("assets to send :" + assets.toString());
        List<AssetSettlementDate> assetSettlementDates = settlementDateService.send(assets);
        log.info("settlementDate received :" + assetSettlementDates);
        for(AssetSettlementDate assetSettlementDate : assetSettlementDates)
        {
            Asset asset = assetService.find(assetSettlementDate.getAssetId());
            if(asset.getSettlementDate() == null)
            {
                SettlementDate root = new SettlementDate();
                root.setSettlementDateId(asset.getAssetId() + "sd");
                asset.setSettlementDate(root);
                assetService.save(asset);
            }
            asset = assetService.find(assetSettlementDate.getAssetId());
            SettlementDate root = service.find(asset.getSettlementDate().getSettlementDateId());
            if(root.getSettlementDates() == null)
                root.setSettlementDates(new HashSet<>());

            boolean found = false;
            for(SettlementDate settlementDate : root.getSettlementDates())
            {
                if(settlementDate.getSettlementDate().equals(assetSettlementDate.getSettlementDate()))
                {
                    settlementDate.setQueryDateTime(LocalDateTime.now());
                    service.save(settlementDate);
                    found = true;
                    log.info("duplicate settlementDate");
                    break;
                }
            }
            if(!found)
            {
                log.info("new settlementDate");
                SettlementDate child = new SettlementDate();
                child.setQueryDateTime(LocalDateTime.now());
                child.setSettlementDateId(root.getSettlementDateId() + root.getSettlementDates().size());
                child.setSettlementDate(assetSettlementDate.getSettlementDate());
                root.getSettlementDates().add(child);
                service.save(root);
            }


        }


    }
}
