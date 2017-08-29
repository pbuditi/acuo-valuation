package com.acuo.valuation.providers.acuo.assets;

import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.ids.AssetId;
import com.acuo.common.model.results.AssetSettlementDate;
import com.acuo.persist.entity.Asset;
import com.acuo.persist.entity.Settlement;
import com.acuo.persist.entity.SettlementDate;
import com.acuo.persist.services.AssetService;
import com.acuo.persist.services.SettlementDateService;
import com.acuo.persist.services.SettlementService;
import com.acuo.valuation.providers.reuters.services.SettlementDateExtractorService;
import com.acuo.valuation.utils.AssetsBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Slf4j
public class SettlementDateProcessor {

    private final AssetService assetService;
    private final SettlementDateExtractorService settlementDateExtractor;
    private final SettlementService settlementService;
    private final SettlementDateService settlementDateService;
    private DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    public SettlementDateProcessor(AssetService assetService,
                                   SettlementDateExtractorService settlementDateExtractor,
                                   SettlementService settlementService,
                                   SettlementDateService settlementDateService) {
        this.assetService = assetService;
        this.settlementDateExtractor = settlementDateExtractor;
        this.settlementService = settlementService;
        this.settlementDateService = settlementDateService;
    }

    public void process() {
        Iterable<Asset> all = assetService.findAll();
        handleBond(all);
        handleCash(all);
    }

    private void handleCash(Iterable<Asset> all) {
        log.info("handling settlement dates for cash ...");
        List<Asset> cash = StreamSupport.stream(all.spliterator(), false)
                .filter(asset -> asset.getType().equalsIgnoreCase("cash"))
                .peek(asset -> {
                    LocalDate now = LocalDate.now();
                    if (now.getDayOfWeek().getValue() > 5)
                        now = now.plusDays(8 - now.getDayOfWeek().getValue());
                    asset.setSettlementTime(df.format(now));
                })
                .collect(toList());
        log.info("saving settlement dates for {} cash assets", cash.size());
        assetService.save(cash);
        log.info("handling settlement dates for cash done");
    }

    private void handleBond(Iterable<Asset> all) {
        log.info("handling settlement dates for bond ...");
        List<Assets> assets = StreamSupport.stream(all.spliterator(), false)
                .filter(asset -> asset.getType().equalsIgnoreCase("bond"))
                .map(AssetsBuilder::buildAssets).collect(toList());
        log.info("sending {} assets for settlement date extraction", assets.size());
        List<AssetSettlementDate> assetSettlementDates = settlementDateExtractor.send(assets);
        log.info("received {} settlement dates", assetSettlementDates.size());
        for (AssetSettlementDate assetSettlementDate : assetSettlementDates) {
            AssetId assetId = AssetId.fromString(assetSettlementDate.getAssetId());
            Settlement settlement = settlementService.getOrCreateSettlementFor(assetId);
            if (settlement.getSettlementDates() == null)
                settlement.setSettlementDates(new HashSet<>());

            boolean found = false;
            for (SettlementDate settlementDate : settlement.getSettlementDates()) {
                if (settlementDate.getSettlementDate().equals(assetSettlementDate.getSettlementDate())) {
                    settlementDate.setQueryDateTime(LocalDateTime.now());
                    settlementDateService.save(settlementDate);
                    found = true;
                    log.debug("duplicate settlementDate");
                    break;
                }
            }
            if (!found) {
                log.debug("new settlementDate");
                SettlementDate child = new SettlementDate();
                child.setQueryDateTime(LocalDateTime.now());
                child.setSettlementDateId(settlement.getSettlementId() + settlement.getSettlementDates().size());
                child.setSettlementDate(assetSettlementDate.getSettlementDate());
                settlement.getSettlementDates().add(child);
                Asset asset = settlement.getAsset();
                asset.setSettlementTime(df.format(assetSettlementDate.getSettlementDate()));
                assetService.save(asset);
                settlementService.save(settlement);
            }
        }
        log.info("handling settlement dates for bond done");
    }
}